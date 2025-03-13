package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.example.hvvs.model.*;
import org.example.hvvs.modules.common.service.AuditLogService;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.FilterMeta;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

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
    @EJB
    private AuditLogService auditLogService;

    private LazyDataModel<Users> lazyUsersModel;
    private String globalFilter;
    private List<Users> filteredUsers;
    private Users newUser;
    private List<Users> selectedUsers;
    private ResidentProfiles residentProfile;
    private SecurityStaffProfiles securityStaffProfile;
    private ManagingStaffProfiles managingStaffProfile;
    private Users editingUser;
    private String exportFormat = "xlsx"; // Default format

    @PostConstruct
    public void init() {
        initializeLazyModel();
        newUser = new Users();
        residentProfile = new ResidentProfiles();
        securityStaffProfile = new SecurityStaffProfiles();
        managingStaffProfile = new ManagingStaffProfiles();
        editingUser = new Users();
        
        // Log page access when controller is initialized
        try {
            Users currentUser = getCurrentUser();
            if (currentUser != null) {
                HttpServletRequest request = getHttpServletRequest();
                auditLogService.logRead(
                    currentUser,
                    "USERS_LIST",
                    null,
                    "Accessed users management page",
                    request
                );
            }
        } catch (Exception e) {
            // Silent catch - don't disrupt the UI for logging errors
        }
    }

    private void initializeLazyModel() {
        lazyUsersModel = new LazyDataModel<Users>() {
            @Override
            public int count(Map<String, FilterMeta> map) {
                return 0;
            }

            @Override
            public List<Users> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                List<Users> results = usersFacade.findRange(
                    first, 
                    pageSize, 
                    globalFilter,
                    sortBy
                );
                lazyUsersModel.setRowCount(usersFacade.count(globalFilter));
                return results;
            }
            
            @Override
            public Users getRowData(String rowKey) {
                return usersFacade.find(Integer.valueOf(rowKey));
            }
            
            @Override
            public String getRowKey(Users user) {
                return String.valueOf(user.getId());
            }
        };
    }

    public void onRoleChange() {
        switch (newUser.getRole()) {
            case RESIDENT:
                if (residentProfile == null) {
                    residentProfile = new ResidentProfiles();
                }
                securityStaffProfile = null;
                managingStaffProfile = null;
                break;
            case SECURITY_STAFF:
                if (securityStaffProfile == null) {
                    securityStaffProfile = new SecurityStaffProfiles();
                }
                residentProfile = null;
                managingStaffProfile = null;
                break;
            case MANAGING_STAFF:
            case SUPER_ADMIN:
                if (managingStaffProfile == null) {
                    managingStaffProfile = new ManagingStaffProfiles();
                }
                residentProfile = null;
                securityStaffProfile = null;
                break;
        }
    }

    public void createUser() {
        try {
            // Get current user from session for audit logging
            Users currentUser = getCurrentUser();
            HttpServletRequest request = getHttpServletRequest();
            
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

            // Create profile based on role
            switch (newUser.getRole()) {
                case RESIDENT:
                    if (residentProfile != null) {
                        residentProfile.setUserId(createdUser);
                        residentProfilesFacade.create(residentProfile);
                    }
                    break;

                case SECURITY_STAFF:
                    if (securityStaffProfile != null) {
                        securityStaffProfile.setUserId(createdUser);
                        securityStaffProfilesFacade.create(securityStaffProfile);
                    }
                    break;

                case MANAGING_STAFF:
                    if (managingStaffProfile != null) {
                        managingStaffProfile.setUserId(createdUser);
                        managingStaffProfilesFacade.create(managingStaffProfile);
                    }
                    break;
            }

            // Log the user creation
            auditLogService.logCreate(
                currentUser,
                "USERS",
                createdUser.getId().toString(),
                "Created new user with username: " + createdUser.getUsername(),
                "{\"username\":\"" + createdUser.getUsername() + 
                "\",\"email\":\"" + createdUser.getEmail() + 
                "\",\"role\":\"" + createdUser.getRole() + "\"}",
                request
            );

            lazyUsersModel.setRowCount(usersFacade.count(globalFilter)); // Refresh count
            newUser = new Users(); // Reset the form
            residentProfile = new ResidentProfiles(); // Reset profiles
            securityStaffProfile = new SecurityStaffProfiles();
            managingStaffProfile = new ManagingStaffProfiles();
            addMessage("Success", "User created successfully");
        } catch (Exception e) {
            addErrorMessage("Error creating user: " + e.getMessage());
        }
    }

    public void deleteSelectedUsers() {
        try {
            // Get current user from session for audit logging
            Users currentUser = getCurrentUser();
            HttpServletRequest request = getHttpServletRequest();
            
            if (selectedUsers != null && !selectedUsers.isEmpty()) {
                Users currentSessionUser = (Users) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
                
                // Filter out current user from deletion
                List<Users> usersToDelete = selectedUsers.stream()
                    .filter(user -> !user.getId().equals(currentSessionUser.getId()))
                    .toList();

                if (usersToDelete.isEmpty()) {
                    addErrorMessage("Cannot delete currently logged in user.");
                    return;
                }

                for (Users user : usersToDelete) {
                    // Log before deletion to capture user details
                    auditLogService.logDelete(
                        currentUser,
                        "USERS",
                        user.getId().toString(),
                        "Deleted user with username: " + user.getUsername(),
                        "{\"username\":\"" + user.getUsername() + 
                        "\",\"email\":\"" + user.getEmail() + 
                        "\",\"role\":\"" + user.getRole() + "\"}",
                        request
                    );
                    
                    // Revoke all sessions first
                    sessionService.revokeAllSessions(user.getId());
                    usersFacade.remove(user);
                }

                lazyUsersModel.setRowCount(usersFacade.count(globalFilter)); // Refresh count

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
                || users.getRole().toString().toLowerCase().contains(filterText);
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
        this.editingUser = user;
        
        // Log user details view
        try {
            Users currentUser = getCurrentUser();
            if (currentUser != null) {
                HttpServletRequest request = getHttpServletRequest();
                auditLogService.logRead(
                    currentUser,
                    "USERS",
                    user.getId().toString(),
                    "Viewed details for user: " + user.getUsername(),
                    request
                );
            }
        } catch (Exception e) {
            // Silent catch - don't disrupt the UI for logging errors
        }
    }

    public void updateUser() {
        try {
            // Get current user from session for audit logging
            Users currentUser = getCurrentUser();
            HttpServletRequest request = getHttpServletRequest();
            
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

            // Prepare old values for audit logging
            String oldValues = "{\"username\":\"" + existingUser.getUsername() + 
                "\",\"email\":\"" + existingUser.getEmail() + 
                "\",\"firstName\":\"" + existingUser.getFirstName() + 
                "\",\"lastName\":\"" + existingUser.getLastName() + 
                "\",\"phoneNumber\":\"" + existingUser.getPhoneNumber() + 
                "\",\"role\":\"" + existingUser.getRole() + "\"}";
                
            // Update the user
            usersFacade.edit(editingUser);
            
            // Prepare new values for audit logging
            String newValues = "{\"username\":\"" + editingUser.getUsername() + 
                "\",\"email\":\"" + editingUser.getEmail() + 
                "\",\"firstName\":\"" + editingUser.getFirstName() + 
                "\",\"lastName\":\"" + editingUser.getLastName() + 
                "\",\"phoneNumber\":\"" + editingUser.getPhoneNumber() + 
                "\",\"role\":\"" + editingUser.getRole() + "\"}";
            
            // Log the update
            auditLogService.logUpdate(
                currentUser,
                "USERS",
                editingUser.getId().toString(),
                "Updated user with username: " + editingUser.getUsername(),
                oldValues,
                newValues,
                request
            );
            
            lazyUsersModel.setRowCount(usersFacade.count(globalFilter)); // Refresh count
            
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
    public LazyDataModel<Users> getLazyUsersModel() {
        return lazyUsersModel;
    }

    public List<Users> getFilteredUsers() {
        return filteredUsers;
    }

    public void setFilteredUsers(List<Users> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public String getGlobalFilter() {
        return globalFilter;
    }
    
    public void setGlobalFilter(String globalFilter) {
        this.globalFilter = globalFilter;
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

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    /**
     * Get the current user from the session
     */
    private Users getCurrentUser() {
        return (Users) FacesContext.getCurrentInstance()
            .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
    }
    
    /**
     * Get the current HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) FacesContext.getCurrentInstance()
            .getExternalContext().getRequest();
    }
} 