package org.example.hvvs.modules.resident.controller;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.model.User;
import org.example.hvvs.modules.resident.services.SettingsService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.DigestUtils;

import java.io.Serializable;
import java.sql.Timestamp;

@Named("SettingsControllerResident")
@SessionScoped
public class SettingsController implements Serializable {

    private User user;
    private ResidentProfile residentProfile;
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;

    @Inject
    private SettingsService generalSettingsService;

    @PostConstruct
    public void init() {
        // Example: load user by some logic, e.g., from session or a dummy userId=1
        User currentUser = (User) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);

        if (currentUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "User not authenticated"));
            return;
        }

        this.user = generalSettingsService.findUserById(currentUser.getId());
        this.residentProfile = generalSettingsService.findResidentProfileByUserId(currentUser.getId());
    }

    /**
     * Action method to save personal information
     */
    @Transactional
    public String savePersonalInformation() {
        try {
            generalSettingsService.updateUser(user);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success",
                            "Your personal information has been saved."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "An error occurred while saving your information. Please try again later."));
        }

        // 5. Return to the same page or navigate to another outcome
        return null;
    }

    /**
     * Action method to update username
     */
    @Transactional
    public String updateUsername() {
        try {
            // Basic validation
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Username cannot be empty."));
                return null;
            }

            // Check if username is already taken
            if (generalSettingsService.isUsernameExists(user.getUsername(), user.getId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "This username is already taken."));
                return null;
            }

            // Update timestamp
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            
            // Save changes
            generalSettingsService.updateUser(user);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success",
                            "Your username has been updated."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "An error occurred while updating your username. Please try again later."));
        }

        return null;
    }

    /**
     * Action method to update password
     */
    @Transactional
    public String updatePassword() {
        try {
            // Basic validation
            if (oldPassword == null || oldPassword.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty() ||
                confirmNewPassword == null || confirmNewPassword.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "All password fields are required."));
                return null;
            }

            // Verify old password
            String oldPassDigest = DigestUtils.sha256Digest(user.getSalt() + oldPassword);
            if (!user.getPassword().equals(oldPassDigest)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Current password is incorrect."));
                return null;
            }

            // Verify new password matches confirmation
            if (!newPassword.equals(confirmNewPassword)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "New passwords do not match."));
                return null;
            }

            // Validate new password strength (you can add more rules)
            if (newPassword.length() < 8) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "New password must be at least 8 characters long."));
                return null;
            }

            // Update the password
            String newPassDigest = DigestUtils.sha256Digest(user.getSalt() + newPassword);
            user.setPassword(newPassDigest);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            
            // Save changes
            generalSettingsService.updateUser(user);

            // Clear password fields
            oldPassword = null;
            newPassword = null;
            confirmNewPassword = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success",
                            "Your password has been updated."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "An error occurred while updating your password. Please try again later."));
        }

        return null;
    }

    /* Getters and setters */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ResidentProfile getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfile residentProfile) {
        this.residentProfile = residentProfile;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
