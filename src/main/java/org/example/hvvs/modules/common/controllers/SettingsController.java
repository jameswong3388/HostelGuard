package org.example.hvvs.modules.common.controllers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.*;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.modules.common.service.MediaService;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.CustomPart;
import org.example.hvvs.utils.DigestUtils;
import org.primefaces.model.file.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.example.hvvs.model.Users.Role.*;

@Named("SettingsController")
@SessionScoped
public class SettingsController implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Common dependencies
    @EJB
    private UsersFacade usersFacade;
    @EJB
    private MediaService mediaService;
    @EJB
    private SessionService sessionService;
    @EJB
    private AuthServices authServices;
    @EJB
    private MfaMethodsFacade mfaMethodsFacade;

    // Profile facades
    @EJB
    private ResidentProfilesFacade residentProfilesFacade;
    @EJB
    private SecurityStaffProfilesFacade securityStaffProfilesFacade;
    @EJB
    private ManagingStaffProfilesFacade managingStaffProfilesFacade;

    // User & Profile
    private Users user;
    private Object profile;

    // Password Management
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;

    // Profile Image
    private Medias profileImage;
    private UploadedFile tempUploadedFile;

    // MFA - General
    private MfaMethods.MfaMethodType selectedPrimaryMethod;

    // MFA - TOTP
    private String totpCode;
    private String qrCodeUrl;
    private String tempSecret;
    private List<String> recoveryCodes;
    private final Gson gson = new Gson();

    // MFA - Email/SMS
    private String emailVerificationCode;
    private String smsVerificationCode;

    // Session Management
    private List<UserSessions> sessions;
    private UUID currentSessionId;

    // Role-specific initialization
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

        this.user = usersFacade.find(currentUser.getId());
        loadProfile();
        loadProfileImage();
    }

    // ---------------------------------
    // General Profile/Image
    // ---------------------------------

    private void loadProfile() {
        switch (user.getRole()) {
            case RESIDENT:
                profile = residentProfilesFacade.find(user.getId());
                break;
            case SECURITY_STAFF:
                profile = securityStaffProfilesFacade.find(user.getId());
                break;
            case MANAGING_STAFF:
            case SUPER_ADMIN: // SUPER_ADMIN shares managing staff profile
                profile = managingStaffProfilesFacade.find(user.getId());
                break;
            default:
                profile = null;
        }
    }

    // Common profile methods
    private void loadProfileImage() {
        List<Medias> profileMedias = mediaService.findByModelAndModelId("user", user.getId().toString());
        this.profileImage = profileMedias.isEmpty() ? null : profileMedias.getFirst();
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
            mediaService.uploadFile(part, "user", userId, "profile-pictures");
            loadProfileImage();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile image uploaded successfully."));
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to upload profile image: " + e.getMessage()));
        }
    }

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

            if (usersFacade.isEmailExists(user.getEmail()) &&
                    !user.getEmail().equalsIgnoreCase(usersFacade.find(user.getId()).getEmail())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "This email is already registered to another account."));
                resetUserData();
                return null;
            }

            // Handle common fields
            usersFacade.edit(user);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated successfully"));
        } catch (Exception e) {
            // Error handling
        }
        return null;
    }

    private void resetUserData() {
        Users currentUser = (Users) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(CommonParam.SESSION_SELF);

        if (currentUser != null) {
            this.user = usersFacade.find(currentUser.getId());
            switch (user.getRole()) {
                case RESIDENT:
                    this.profile = residentProfilesFacade.find(currentUser.getId());
                    break;
                case SECURITY_STAFF:
                    this.profile = securityStaffProfilesFacade.find(currentUser.getId());
                    break;
                case MANAGING_STAFF:
                case SUPER_ADMIN:
                    this.profile = managingStaffProfilesFacade.find(currentUser.getId());
                    break;
            }
        }
    }

    // ---------------------------------
    // Username & Password
    // ---------------------------------

    @Transactional
    public String updateUsername() {
        try {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Username cannot be empty."));
                resetUserData();
                return null;
            }

            if (usersFacade.isUsernameExists(user.getUsername()) &&
                    !user.getUsername().equalsIgnoreCase(usersFacade.find(user.getId()).getUsername())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "This username is already taken."));
                resetUserData();
                return null;
            }

            usersFacade.edit(user);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Your username has been updated."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "An error occurred while updating your username. Please try again later."));
            resetUserData();
        }
        return null;
    }

    @Transactional
    public String updatePassword() {
        try {
            // Basic validation
            if (oldPassword == null || oldPassword.trim().isEmpty() ||
                    newPassword == null || newPassword.trim().isEmpty() ||
                    confirmNewPassword == null || confirmNewPassword.trim().isEmpty()) {

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "All password fields are required."));
                resetUserData();
                return null;
            }

            // Verify old password
            String oldPassDigest = DigestUtils.sha256Digest(user.getSalt() + oldPassword);
            if (!user.getPassword().equals(oldPassDigest)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "Current password is incorrect."));
                resetUserData();
                return null;
            }

            // Verify new password matches confirmation
            if (!newPassword.equals(confirmNewPassword)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "New passwords do not match."));
                resetUserData();
                return null;
            }

            // Validate new password strength
            if (newPassword.length() < 8) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", "New password must be at least 8 characters long."));
                resetUserData();
                return null;
            }

            // Update the password
            String newPassDigest = DigestUtils.sha256Digest(user.getSalt() + newPassword);
            user.setPassword(newPassDigest);
            usersFacade.edit(user);

            oldPassword = null;
            newPassword = null;
            confirmNewPassword = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "Your password has been updated."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "An error occurred while updating your password. Please try again later."));
            resetUserData();
        }
        return null;
    }

    // ---------------------------------
    // TOTP (Google Authenticator)
    // ---------------------------------

    public void initializeTOTP2FA() {
        try {
            // Generate secret & backup codes from AuthServices
            tempSecret = authServices.generateTotpSecret();
            recoveryCodes = authServices.generateRecoveryCodes();

            // Build the otpauth:// URL for QR code usage
            String encodedIssuer = URLEncoder.encode("HostelGuard™", StandardCharsets.UTF_8);
            String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            qrCodeUrl = String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                    encodedIssuer, encodedEmail, tempSecret, encodedIssuer
            );
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to initialize 2FA: " + e.getMessage()));
        }
    }

    @Transactional
    public void verifyAndEnableTOTP() {
        try {
            if (totpCode == null || totpCode.trim().isEmpty()) {
                totpCode = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Invalid verification code. Please try again."));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // Verify the provided TOTP code using AuthServices
            boolean isCodeValid = authServices.verifyTotpCode(tempSecret, Integer.parseInt(totpCode));

            if (!isCodeValid) {
                totpCode = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Invalid verification code. Please try again."));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // Create the MFA entity (this automatically updates user.is_mfa_enable if it's the first method)
            authServices.createMFA(user, MfaMethods.MfaMethodType.TOTP, tempSecret, recoveryCodes);

            // Clear sensitive data
            tempSecret = null;
            totpCode = null;
            qrCodeUrl = null;
            recoveryCodes = null;

            // Refresh user
            user = usersFacade.find(user.getId());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Two-factor authentication (TOTP) has been enabled."));

        } catch (NumberFormatException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Invalid verification code format. Please enter only numbers."));
            FacesContext.getCurrentInstance().validationFailed();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to enable TOTP 2FA: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();

        }
    }

    @Transactional
    public void disableTOTP() {
        try {
            // Delegate disabling logic to AuthServices
            authServices.disableMFA(user, MfaMethods.MfaMethodType.TOTP);

            // Clear any UI-cached references
            sessions = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Two-factor authentication (TOTP) has been disabled."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to disable TOTP 2FA: " + e.getMessage()));
        }
    }

    public boolean getHasTOTPEnabled() {
        try {
            if (user == null) return false;

            // Always reload fresh user
            Users freshUser = usersFacade.find(user.getId());
            if (!Boolean.TRUE.equals(freshUser.getIs_mfa_enable())) return false;

            List<MfaMethods> mfaMethods = mfaMethodsFacade.findEnabledMfaMethodsByUser(freshUser);
            return mfaMethods.stream()
                    .anyMatch(method -> method.getMethod() == MfaMethods.MfaMethodType.TOTP
                            && Boolean.TRUE.equals(method.getEnabled()));
        } catch (Exception e) {
            return false;
        }
    }

    public String getPreferredMfaStatus() {
        List<MfaMethods> enabledMethods = mfaMethodsFacade.findEnabledMfaMethodsByUser(user)
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getEnabled()))
                .toList();

        if (enabledMethods.isEmpty()) {
            return "Disabled";
        }

        long primaryCount = enabledMethods.stream()
                .filter(MfaMethods::getPrimary)
                .count();

        if (enabledMethods.size() == 1) {
            return "Enabled (" + enabledMethods.getFirst().getMethod().toString() + ")";
        }

        if (primaryCount != 1) {
            return "Multiple methods - needs configuration";
        }

        MfaMethods primary = enabledMethods.stream()
                .filter(MfaMethods::getPrimary)
                .findFirst()
                .orElseThrow();

        return "Enabled (" + primary.getMethod().toString() + " as primary)";
    }

    public void updatePrimaryMethod() {
        try {
            if (selectedPrimaryMethod == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Please select a method to set as primary"));
                return;
            }

            List<MfaMethods> allMethods = mfaMethodsFacade.findEnabledMfaMethodsByUser(user);

            // Update all methods
            for (MfaMethods method : allMethods) {
                boolean shouldBePrimary = method.getMethod() == selectedPrimaryMethod;
                method.setPrimary(shouldBePrimary);
                mfaMethodsFacade.edit(method);
            }

            // Clear the selection
            selectedPrimaryMethod = null;

            // Refresh the user data
            user = usersFacade.find(user.getId());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Primary authentication method updated"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to update primary method: " + e.getMessage()));
        }
    }

    public List<MfaMethods> getEnabledMfaMethods() {
        return mfaMethodsFacade.findEnabledMfaMethodsByUser(user)
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getEnabled()))
                .toList();
    }

    // ---------------------------------
    // Email / SMS MFA (placeholders)
    // ---------------------------------

    public void initializeSMS2FA() {
        try {
            // Find or create email MFA method
            MfaMethods smsMethod = mfaMethodsFacade.findMfaMethodsByUser(user)
                    .stream()
                    .filter(m -> m.getMethod() == MfaMethods.MfaMethodType.SMS)
                    .findFirst()
                    .orElseGet(() -> {
                        MfaMethods newMethod = new MfaMethods();
                        newMethod.setUser(user);
                        newMethod.setMethod(MfaMethods.MfaMethodType.SMS);
                        newMethod.setEnabled(false);
                        mfaMethodsFacade.create(newMethod);
                        return newMethod;
                    });

            // Send verification code via email
            authServices.sendSMSCode(smsMethod);

            // Generate recovery codes
            recoveryCodes = authServices.generateRecoveryCodes();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to initialize email 2FA: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    @Transactional
    public void verifyAndEnableSMSVerification() {
        try {
            MfaMethods smsMethod = mfaMethodsFacade.findMfaMethodsByUser(user)
                    .stream()
                    .filter(m -> m.getMethod() == MfaMethods.MfaMethodType.SMS)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("SMS method not found"));

            boolean isCodeValid = authServices.verifySMSCode(smsMethod, smsVerificationCode);

            if (!isCodeValid) {
                smsVerificationCode = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Invalid verification code"));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // Enable the method and set recovery codes
            smsMethod.setEnabled(true);
            smsMethod.setRecoveryCodes(gson.toJson(recoveryCodes));

            // Set as primary if first MFA method
            MfaMethods primaryMfaMethodByUser = mfaMethodsFacade.findPrimaryMfaMethodByUser(user);
            smsMethod.setPrimary(primaryMfaMethodByUser == null);
            mfaMethodsFacade.edit(smsMethod);

            // Update user MFA status if first method
            if (primaryMfaMethodByUser == null) {
                user.setIs_mfa_enable(true);
                usersFacade.edit(user);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Email 2FA enabled successfully"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Verification failed: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();

        }
    }

    public void initializeEmail2FA() {
        try {
            // Find or create email MFA method
            MfaMethods emailMethod = mfaMethodsFacade.findMfaMethodsByUser(user)
                    .stream()
                    .filter(m -> m.getMethod() == MfaMethods.MfaMethodType.EMAIL)
                    .findFirst()
                    .orElseGet(() -> {
                        MfaMethods newMethod = new MfaMethods();
                        newMethod.setUser(user);
                        newMethod.setMethod(MfaMethods.MfaMethodType.EMAIL);
                        newMethod.setEnabled(false);
                        mfaMethodsFacade.create(newMethod);
                        return newMethod;
                    });

            // Send verification code via email
            authServices.sendEmailCode(emailMethod);

            // Generate recovery codes
            recoveryCodes = authServices.generateRecoveryCodes();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to initialize email 2FA: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    @Transactional
    public void verifyAndEnableEmailVerification() {
        try {
            MfaMethods emailMethod = mfaMethodsFacade.findMfaMethodsByUser(user)
                    .stream()
                    .filter(m -> m.getMethod() == MfaMethods.MfaMethodType.EMAIL)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Email method not found"));

            boolean isCodeValid = authServices.verifyEmailCode(emailMethod, emailVerificationCode);

            if (!isCodeValid) {
                emailVerificationCode = null;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Invalid verification code"));
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // Enable the method and set recovery codes
            emailMethod.setEnabled(true);
            emailMethod.setRecoveryCodes(gson.toJson(recoveryCodes));

            // Set as primary if first MFA method
            MfaMethods primaryMfaMethodByUser = mfaMethodsFacade.findPrimaryMfaMethodByUser(user);
            emailMethod.setPrimary(primaryMfaMethodByUser == null);
            mfaMethodsFacade.edit(emailMethod);

            // Update user MFA status if first method
            if (primaryMfaMethodByUser == null) {
                user.setIs_mfa_enable(true);
                usersFacade.edit(user);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Email 2FA enabled successfully"));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Verification failed: " + e.getMessage()));
            FacesContext.getCurrentInstance().validationFailed();

        }
    }

    @Transactional
    public void disableEmail2FA() {
        try {
            authServices.disableMFA(user, MfaMethods.MfaMethodType.EMAIL);
            emailVerificationCode = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Email two-factor authentication has been disabled."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to disable Email 2FA: " + e.getMessage()));
        }
    }

    @Transactional
    public void disableSMS2FA() {
        try {
            authServices.disableMFA(user, MfaMethods.MfaMethodType.SMS);
            smsVerificationCode = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "SMS two-factor authentication has been disabled."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to disable SMS 2FA: " + e.getMessage()));
        }
    }

    public List<String> getRecoveryCodes() {
        if (recoveryCodes != null && !recoveryCodes.isEmpty()) {
            return recoveryCodes;
        }

        MfaMethods primaryMethod = mfaMethodsFacade.findEnabledMfaMethodsByUser(user)
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getEnabled()) && Boolean.TRUE.equals(m.getPrimary()))
                .findFirst()
                .orElse(null);

        return primaryMethod != null && primaryMethod.getRecoveryCodes() != null ?
                gson.fromJson(primaryMethod.getRecoveryCodes(), new TypeToken<List<String>>() {
                }.getType()) :
                Collections.emptyList();
    }


    public boolean getHasEmailEnabled() {
        try {
            if (user == null) return false;
            Users freshUser = usersFacade.find(user.getId());
            if (!Boolean.TRUE.equals(freshUser.getIs_mfa_enable())) return false;

            return mfaMethodsFacade.findEnabledMfaMethodsByUser(freshUser)
                    .stream()
                    .anyMatch(method -> method.getMethod() == MfaMethods.MfaMethodType.EMAIL
                            && Boolean.TRUE.equals(method.getEnabled()));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean getHasSMSEnabled() {
        try {
            if (user == null) return false;
            Users freshUser = usersFacade.find(user.getId());
            if (!Boolean.TRUE.equals(freshUser.getIs_mfa_enable())) return false;

            return mfaMethodsFacade.findEnabledMfaMethodsByUser(freshUser)
                    .stream()
                    .anyMatch(method -> method.getMethod() == MfaMethods.MfaMethodType.SMS
                            && Boolean.TRUE.equals(method.getEnabled()));
        } catch (Exception e) {
            return false;
        }
    }

    // ---------------------------------
    // Session Management
    // ---------------------------------

    public void reloadSessions() {
        sessions = null;
        getSessions();
    }

    public List<UserSessions> getSessions() {
        if (sessions == null) {
            Users currentUser = (Users) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
            sessions = sessionService.getActiveSessions(currentUser);

            // Example: parse location for each session
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
        sessions = null;
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage("Session revoked successfully"));
    }

    public String revokeAllSessions() {
        Users currentUser = (Users) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
        sessionService.revokeAllSessions(currentUser.getId());
        sessions = null;

        // Invalidate current session
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/auth/sign-in.xhtml?faces-redirect=true";
    }

    // ---------------------------------
    // Helpers
    // ---------------------------------

    public boolean isManagingStaff() {
        return user.getRole() == MANAGING_STAFF || user.getRole() == SUPER_ADMIN;
    }

    public boolean isResident() {
        return user.getRole() == RESIDENT;
    }

    public boolean isSecurityStaff() {
        return user.getRole() == SECURITY_STAFF;
    }

    // ---------------------------------
    // Getters / Setters
    // ---------------------------------

    public ResidentProfiles getResidentProfile() {
        return isResident() ? (ResidentProfiles) profile : null;
    }

    public SecurityStaffProfiles getSecurityStaffProfile() {
        return isSecurityStaff() ? (SecurityStaffProfiles) profile : null;
    }

    public ManagingStaffProfiles getManagingStaffProfile() {
        return isManagingStaff() ? (ManagingStaffProfiles) profile : null;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
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

    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public MfaMethods.MfaMethodType getSelectedPrimaryMethod() {
        return selectedPrimaryMethod;
    }

    public void setSelectedPrimaryMethod(MfaMethods.MfaMethodType selectedPrimaryMethod) {
        this.selectedPrimaryMethod = selectedPrimaryMethod;
    }

    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }

    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }

    public String getSmsVerificationCode() {
        return smsVerificationCode;
    }

    public void setSmsVerificationCode(String smsVerificationCode) {
        this.smsVerificationCode = smsVerificationCode;
    }

    public UUID getCurrentSessionId() {
        return currentSessionId;
    }
}