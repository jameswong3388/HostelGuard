package org.example.hvvs.modules.resident.controller;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.example.hvvs.commonClasses.CustomPart;
import org.example.hvvs.model.Medias;
import org.example.hvvs.model.ResidentProfiles;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.common.service.MediaService;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.modules.resident.services.SettingsServiceResident;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.DigestUtils;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Named("SettingsControllerResident")
@SessionScoped
public class SettingsControllerResident implements Serializable {

    private Users user;
    private ResidentProfiles residentProfile;
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
    private Medias profileImage;
    private UploadedFile tempUploadedFile;

    @Inject
    private SettingsServiceResident generalSettingsService;

    @Inject
    private MediaService mediaService;

    @EJB
    private SessionService sessionService;

    private List<UserSessions> sessions;
    private UUID currentSessionId;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        this.currentSessionId = (UUID) session.getAttribute(CommonParam.SESSION_ID);

        Users currentUser = (Users) FacesContext.getCurrentInstance()
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
        loadProfileImage(); // Load existing profile image
    }

    private void loadProfileImage() {
        List<Medias> profileMedias = mediaService.findByModelAndModelId("user", user.getId().toString());
        if (!profileMedias.isEmpty()) {
            this.profileImage = profileMedias.getFirst();
        } else {
            this.profileImage = null;
        }
    }

    public void uploadProfileImage() {
        if (tempUploadedFile == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No file selected."));
            return;
        }

        try {
            String userId = user.getId().toString();
            // Delete existing profile images
            mediaService.deleteByModelAndModelId("user", userId);

            InputStream input = tempUploadedFile.getInputStream();
            CustomPart part = new CustomPart(
                    tempUploadedFile.getFileName(),
                    tempUploadedFile.getContentType(),
                    tempUploadedFile.getSize(),
                    input
            );


            // Upload new image
            Medias media = mediaService.uploadFile(part, "user", userId, "profile-pictures");
            loadProfileImage();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile image uploaded successfully."));
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to upload profile image: " + e.getMessage()));
        }
    }

    /**
     * Action method to save personal information
     */
    @Transactional
    public String savePersonalInformation() {
        try {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Email cannot be empty."));
                resetUserData();
                return null;
            }

            // Check if email is already taken by another user
            if (generalSettingsService.isEmailExists(user.getEmail(), user.getId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "This email is already registered to another account."));
                resetUserData();
                return null;
            }

            // Update timestamp
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

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
            resetUserData();
        }

        return null;
    }

    /**
     * Helper method to reset user data from database
     */
    private void resetUserData() {
        Users currentUser = (Users) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);

        if (currentUser != null) {
            this.user = generalSettingsService.findUserById(currentUser.getId());
            this.residentProfile = generalSettingsService.findResidentProfileByUserId(currentUser.getId());
        }
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
                resetUserData();
                return null;
            }

            // Check if username is already taken
            if (generalSettingsService.isUsernameExists(user.getUsername(), user.getId())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "This username is already taken."));
                resetUserData();
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
            resetUserData();
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
                resetUserData();
                return null;
            }

            // Verify old password
            String oldPassDigest = DigestUtils.sha256Digest(user.getSalt() + oldPassword);
            if (!user.getPassword().equals(oldPassDigest)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Current password is incorrect."));
                resetUserData();
                return null;
            }

            // Verify new password matches confirmation
            if (!newPassword.equals(confirmNewPassword)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "New passwords do not match."));
                resetUserData();
                return null;
            }

            // Validate new password strength (you can add more rules)
            if (newPassword.length() < 8) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "New password must be at least 8 characters long."));
                resetUserData();
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
            resetUserData();
        }

        return null;
    }

    public void reloadSessions() {
        sessions = null; // Force refresh on next access
        getSessions(); // Explicitly reload
    }

    public List<UserSessions> getSessions() {
        if (sessions == null) {
            Users currentUser = (Users) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
            sessions = sessionService.getActiveSessions(currentUser);

            // Add location parsing for each session
            sessions.forEach(session -> {
                String location = sessionService.parseLocationFromIp(session.getIpAddress());
                String[] parts = location.split(", ");
                if (parts.length == 3) {
                    session.setCity(parts[0]);
                    session.setRegion(parts[1]);
                    session.setCountry(parts[2]);
                }
            });
        }
        return sessions;
    }

    public void revokeSession(UUID sessionId) {
        sessionService.revokeSession(sessionId);
        sessions = null; // Refresh session list
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Session revoked successfully"));
    }

    public String revokeAllSessions() {
        Users currentUser = (Users) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
        sessionService.revokeAllSessions(currentUser.getId());
        sessions = null;

        // Invalidate current session and redirect
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/auth.xhtml?faces-redirect=true";
    }

    // Add viewSessionDetails method if needed
    public void viewSessionDetails(UserSessions session) {
        // Implementation for viewing session details
    }

    public UUID getCurrentSessionId() {
        return currentSessionId;
    }

    /* Getters and setters */
    // Update getter/setter
    public Medias getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(Medias profileImage) {
        this.profileImage = profileImage;
    }

    public UploadedFile getTempUploadedFile() {
        return tempUploadedFile;
    }

    public void setTempUploadedFile(UploadedFile tempUploadedFile) {
        this.tempUploadedFile = tempUploadedFile;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public ResidentProfiles getResidentProfile() {
        return residentProfile;
    }

    public void setResidentProfile(ResidentProfiles residentProfile) {
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
