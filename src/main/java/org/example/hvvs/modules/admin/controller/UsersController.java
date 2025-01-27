package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.hvvs.model.User;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.model.SecurityStaffProfile;
import org.example.hvvs.model.ManagingStaffProfile;
import org.example.hvvs.modules.admin.service.UsersService;
import org.primefaces.event.RowEditEvent;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

@Named
@ViewScoped
public class UsersController implements Serializable {
    
    @Inject
    private UsersService userService;
    
    private List<User> users;
    private List<User> filteredUsers;
    private User newUser;
    private List<User> selectedUsers;
    private ResidentProfile residentProfile;
    private SecurityStaffProfile securityStaffProfile;
    private ManagingStaffProfile managingStaffProfile;
    
    @PostConstruct
    public void init() {
        users = userService.getAllUsers();
        newUser = new User();
        residentProfile = new ResidentProfile();
        securityStaffProfile = new SecurityStaffProfile();
        managingStaffProfile = new ManagingStaffProfile();
    }
    
    public void onRoleChange() {
        if ("RESIDENT".equals(newUser.getRole())) {
            if (residentProfile == null) {
                residentProfile = new ResidentProfile();
            }
            securityStaffProfile = null;
            managingStaffProfile = null;
        } else if ("SECURITY_STAFF".equals(newUser.getRole())) {
            if (securityStaffProfile == null) {
                securityStaffProfile = new SecurityStaffProfile();
            }
            residentProfile = null;
            managingStaffProfile = null;
        } else if ("MANAGING_STAFF".equals(newUser.getRole())) {
            if (managingStaffProfile == null) {
                managingStaffProfile = new ManagingStaffProfile();
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
            if (userService.isUsernameExists(newUser.getUsername())) {
                addErrorMessage("Username already exists");
                return;
            }
            if (userService.isEmailExists(newUser.getEmail())) {
                addErrorMessage("Email already exists");
                return;
            }
            
            User createdUser = userService.createUser(newUser);

            // Create role-specific profile
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            switch (newUser.getRole()) {
                case "RESIDENT":
                    if (residentProfile != null) {
                        residentProfile.setUserId(createdUser);
                        residentProfile.setCreatedAt(now);
                        residentProfile.setUpdatedAt(now);
                        userService.createResidentProfile(residentProfile);
                    }
                    break;
                    
                case "SECURITY_STAFF":
                    if (securityStaffProfile != null) {
                        securityStaffProfile.setUserId(createdUser);
                        securityStaffProfile.setCreatedAt(now);
                        securityStaffProfile.setUpdatedAt(now);
                        userService.createSecurityStaffProfile(securityStaffProfile);
                    }
                    break;
                    
                case "MANAGING_STAFF":
                    if (managingStaffProfile != null) {
                        managingStaffProfile.setUserId(createdUser);
                        managingStaffProfile.setCreatedAt(now);
                        managingStaffProfile.setUpdatedAt(now);
                        userService.createManagingStaffProfile(managingStaffProfile);
                    }
                    break;
            }

            users = userService.getAllUsers(); // Refresh the list
            newUser = new User(); // Reset the form
            residentProfile = new ResidentProfile(); // Reset profiles
            securityStaffProfile = new SecurityStaffProfile();
            managingStaffProfile = new ManagingStaffProfile();
            addMessage("Success", "User created successfully");
        } catch (Exception e) {
            addErrorMessage("Error creating user: " + e.getMessage());
        }
    }
    
    public void onRowEdit(RowEditEvent<User> event) {
        try {
            User editedUser = event.getObject();
            userService.updateUser(editedUser);
            addMessage("Success", "User updated successfully");
        } catch (Exception e) {
            addErrorMessage("Error updating user: " + e.getMessage());
        }
    }
    
    public void onRowCancel(RowEditEvent<User> event) {
        addMessage("Cancelled", "Edit cancelled");
    }

    public void deleteSelectedUsers() {
        try {
            if (selectedUsers != null && !selectedUsers.isEmpty()) {
                for (User user : selectedUsers) {
                    userService.deleteUser(user);
                }
                // Remove from the local list so that the table UI updates
                users.removeAll(selectedUsers);

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

        User user = (User) value;
        return user.getUsername().toLowerCase().contains(filterText)
                || user.getEmail().toLowerCase().contains(filterText)
                || user.getFirstName().toLowerCase().contains(filterText)
                || user.getLastName().toLowerCase().contains(filterText)
                || user.getPhoneNumber().toLowerCase().contains(filterText)
                || user.getRole().toLowerCase().contains(filterText);
    }
    
    private void addMessage(String summary, String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    private void addErrorMessage(String detail) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", detail);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    
    // Getters and Setters
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
    
    public List<User> getFilteredUsers() {
        return filteredUsers;
    }
    
    public void setFilteredUsers(List<User> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }
    
    public User getNewUser() {
        return newUser;
    }
    
    public void setNewUser(User newUser) {
        this.newUser = newUser;
    }

    public List<User> getSelectedUsers() {
        return selectedUsers;
    }

    public void setSelectedUsers(List<User> selectedUsers) {
        this.selectedUsers = selectedUsers;
    }

    public void clearSelection() {
        if (selectedUsers != null) {
            selectedUsers.clear();
        }
    }

    public ResidentProfile getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfile residentProfile) {
        this.residentProfile = residentProfile;
    }

    public SecurityStaffProfile getSecurityStaffProfile() {
        return securityStaffProfile;
    }

    public void setSecurityStaffProfile(SecurityStaffProfile securityStaffProfile) {
        this.securityStaffProfile = securityStaffProfile;
    }

    public ManagingStaffProfile getManagingStaffProfile() {
        return managingStaffProfile;
    }

    public void setManagingStaffProfile(ManagingStaffProfile managingStaffProfile) {
        this.managingStaffProfile = managingStaffProfile;
    }
} 