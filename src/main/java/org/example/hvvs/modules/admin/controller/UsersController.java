package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.example.hvvs.model.*;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class UsersController implements Serializable {
    @EJB
    private UsersFacade usersFacade;
    @EJB
    private ResidentProfilesFacade residentProfilesFacade;
    @EJB
    private SecurityStaffProfilesFacade securityStaffProfilesFacade;
    @EJB
    private ManagingStaffProfilesFacade managingStaffProfilesFacade;
    @EJB
    private SessionService sessionService;

    private List<Users> users;
    private List<Users> filteredUsers;
    private Users newUser;
    private List<Users> selectedUsers;
    private ResidentProfiles residentProfile;
    private SecurityStaffProfiles securityStaffProfile;
    private ManagingStaffProfiles managingStaffProfile;
    private Users editingUser;

    @PostConstruct
    public void init() {
        users = usersFacade.findAll();
        newUser = new Users();
        residentProfile = new ResidentProfiles();
        securityStaffProfile = new SecurityStaffProfiles();
        managingStaffProfile = new ManagingStaffProfiles();
        editingUser = new Users();
    }

    public void onRoleChange() {
        if ("RESIDENT".equals(newUser.getRole())) {
            if (residentProfile == null) {
                residentProfile = new ResidentProfiles();
            }
            securityStaffProfile = null;
            managingStaffProfile = null;
        } else if ("SECURITY_STAFF".equals(newUser.getRole())) {
            if (securityStaffProfile == null) {
                securityStaffProfile = new SecurityStaffProfiles();
            }
            residentProfile = null;
            managingStaffProfile = null;
        } else if ("MANAGING_STAFF".equals(newUser.getRole())) {
            if (managingStaffProfile == null) {
                managingStaffProfile = new ManagingStaffProfiles();
            }
            residentProfile = null;
            securityStaffProfile = null;
        } else {
            residentProfile = null;
            securityStaffProfile = null;
            managingStaffProfile = null;
        }
    }

    public void createUser() {
        try {
            // Validate unique constraints
            if (usersFacade.isUsernameExists(newUser.getUsername())) {
                addErrorMessage("Username already exists");
                return;
            }
            if (usersFacade.isEmailExists(newUser.getEmail())) {
                addErrorMessage("Email already exists");
                return;
            }

            Users createdUser = usersFacade.createUser(newUser);

            // Create role-specific profile
            Timestamp now = new Timestamp(System.currentTimeMillis());

            switch (newUser.getRole()) {
                case "RESIDENT":
                    if (residentProfile != null) {
                        residentProfile.setUserId(createdUser);
                        residentProfile.setCreatedAt(now);
                        residentProfile.setUpdatedAt(now);
                        residentProfilesFacade.create(residentProfile);
                    }
                    break;

                case "SECURITY_STAFF":
                    if (securityStaffProfile != null) {
                        securityStaffProfile.setUserId(createdUser);
                        securityStaffProfile.setCreatedAt(now);
                        securityStaffProfile.setUpdatedAt(now);
                        securityStaffProfilesFacade.create(securityStaffProfile);
                    }
                    break;

                case "MANAGING_STAFF":
                    if (managingStaffProfile != null) {
                        managingStaffProfile.setUserId(createdUser);
                        managingStaffProfile.setCreatedAt(now);
                        managingStaffProfile.setUpdatedAt(now);
                        managingStaffProfilesFacade.create(managingStaffProfile);
                    }
                    break;
            }

            users = usersFacade.findAll(); // Refresh the list
            newUser = new Users(); // Reset the form
            residentProfile = new ResidentProfiles(); // Reset profiles
            securityStaffProfile = new SecurityStaffProfiles();
            managingStaffProfile = new ManagingStaffProfiles();
            addMessage("Success", "User created successfully");
        } catch (Exception e) {
            addErrorMessage("Error creating user: " + e.getMessage());
        }
    }

    public void onRowEdit(RowEditEvent<Users> event) {
        try {
            Users editedUser = event.getObject();
            editedUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(editedUser);
            addMessage("Success", "User updated successfully");
        } catch (Exception e) {
            addErrorMessage("Error updating user: " + e.getMessage());
        }
    }

    public void onRowCancel(RowEditEvent<Users> event) {
        addMessage("Cancelled", "Edit cancelled");
    }

    public void deleteSelectedUsers() {
        try {
            if (selectedUsers != null && !selectedUsers.isEmpty()) {
                Users currentUser = (Users) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
                
                // Filter out current user from deletion
                List<Users> usersToDelete = selectedUsers.stream()
                    .filter(user -> !user.getId().equals(currentUser.getId()))
                    .toList();

                if (usersToDelete.isEmpty()) {
                    addErrorMessage("Cannot delete currently logged in user.");
                    return;
                }

                for (Users user : usersToDelete) {
                    // Revoke all sessions first
                    sessionService.revokeAllSessions(user.getId());
                    usersFacade.remove(user);
                }

                // Remove from the local list so that the table UI updates
                users.removeAll(usersToDelete);

                // Clear the selection
                selectedUsers.clear();

                addMessage("Success", "Selected users deleted successfully");
            } else {
                addErrorMessage("No users selected for deletion.");
            }
        } catch (Exception e) {
            addErrorMessage("Error deleting users: " + e.getMessage());
        }
    }

    public String getDeleteSelectedButtonLabel() {
        if (selectedUsers == null || selectedUsers.isEmpty()) {
            return "Delete Selected Users";
        } else {
            return "Delete Selected Users (" + selectedUsers.size() + ")";
        }
    }

    public boolean globalFilterFunction(Object value, Object filter, String filterLocale) {
        String filterText = (filter == null) ? null : filter.toString().trim().toLowerCase();
        if (filterText == null || filterText.isEmpty()) {
            return true;
        }

        Users users = (Users) value;
        return users.getUsername().toLowerCase().contains(filterText)
                || users.getEmail().toLowerCase().contains(filterText)
                || users.getFirstName().toLowerCase().contains(filterText)
                || users.getLastName().toLowerCase().contains(filterText)
                || users.getPhoneNumber().toLowerCase().contains(filterText)
                || users.getRole().toLowerCase().contains(filterText);
    }

    public void clearSelection() {
        if (selectedUsers != null) {
            selectedUsers.clear();
        }
    }

    private void addMessage(String summary, String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    private void addErrorMessage(String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void prepareEdit(Users user) {
        this.editingUser = new Users();
        this.editingUser.setId(user.getId());
        this.editingUser.setUsername(user.getUsername());
        this.editingUser.setEmail(user.getEmail());
        this.editingUser.setPassword(user.getPassword());
        this.editingUser.setSalt(user.getSalt());
        this.editingUser.setFirstName(user.getFirstName());
        this.editingUser.setLastName(user.getLastName());
        this.editingUser.setAddress(user.getAddress());
        this.editingUser.setPhoneNumber(user.getPhoneNumber());
        this.editingUser.setRole(user.getRole());
        this.editingUser.setIsActive(user.getIsActive());
        this.editingUser.setIdentity_number(user.getIdentity_number());
        this.editingUser.setIs_mfa_enable(user.getIs_mfa_enable());
        this.editingUser.setUpdatedAt(user.getUpdatedAt());
        this.editingUser.setCreatedAt(user.getCreatedAt());
    }

    public void updateUser() {
        try {
            Users existingUser = usersFacade.find(editingUser.getId());
            if (!existingUser.getUsername().equals(editingUser.getUsername()) 
                && usersFacade.isUsernameExists(editingUser.getUsername())) {
                addErrorMessage("Username already exists");
                return;
            }
            if (!existingUser.getEmail().equals(editingUser.getEmail()) 
                && usersFacade.isEmailExists(editingUser.getEmail())) {
                addErrorMessage("Email already exists");
                return;
            }

            editingUser.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(editingUser);
            
            users = usersFacade.findAll();
            
            addMessage("Success", "User updated successfully");
        } catch (Exception e) {
            addErrorMessage("Error updating user: " + e.getMessage());
        }
    }

    public Users getEditingUser() {
        return editingUser;
    }

    public void setEditingUser(Users editingUser) {
        this.editingUser = editingUser;
    }

    // Getters and Setters
    public List<Users> getUsers() {
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }

    public List<Users> getFilteredUsers() {
        return filteredUsers;
    }

    public void setFilteredUsers(List<Users> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public Users getNewUser() {
        return newUser;
    }

    public void setNewUser(Users newUser) {
        this.newUser = newUser;
    }

    public List<Users> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<Users> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public ResidentProfiles getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfiles residentProfile) {
        this.residentProfile = residentProfile;
    }

    public SecurityStaffProfiles getSecurityStaffProfile() {
        return securityStaffProfile;
    }

    public void setSecurityStaffProfile(SecurityStaffProfiles securityStaffProfile) {
        this.securityStaffProfile = securityStaffProfile;
    }

    public ManagingStaffProfiles getManagingStaffProfile() {
        return managingStaffProfile;
    }

    public void setManagingStaffProfile(ManagingStaffProfiles managingStaffProfile) {
        this.managingStaffProfile = managingStaffProfile;
    }
} 