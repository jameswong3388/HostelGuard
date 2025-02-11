package org.example.hvvs.modules.admin.controller;

import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.ejb.EJB;
import org.example.hvvs.commonClasses.CustomPart;
import org.example.hvvs.model.*;
import org.example.hvvs.modules.common.service.MediaService;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.DigestUtils;
import org.primefaces.model.file.UploadedFile;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

@Named("SettingsControllerAdmin")
@SessionScoped
public class SettingsControllerAdmin implements Serializable {

    // User & Profile
    private Users user;
    private ManagingStaffProfiles managingStaffProfile;

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
    private List<String> backupCodes;
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private final Gson gson = new Gson();

    // MFA - Email/SMS
    private boolean hasEmailEnabled;
    private boolean hasSMSEnabled;
    private String emailVerificationCode;
    private String smsVerificationCode;

    // Session Management
    private List<UserSessions> sessions;
    private UUID currentSessionId;

    // Injected Services
    @EJB
    private MediaService mediaService;
    @EJB
    private SessionService sessionService;

    @EJB
    private UsersFacade usersFacade;
    @EJB
    private ManagingStaffProfilesFacade managingStaffProfilesFacade;
    @EJB
    private MfaMethodsFacade mfaMethodsFacade;

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
        this.managingStaffProfile = managingStaffProfilesFacade.find(currentUser.getId());
        loadProfileImage();
    }

    // General.xhtml

    /**
     * Action method to save personal information
     */
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

    @Transactional
    public String savePersonalInformation() {
        try {
            // Basic validation
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Email cannot be empty."));
                resetUserData();
                return null;
            }

            // Check if email is already taken by another user
            if (usersFacade.isEmailExists(user.getEmail())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "This email is already registered to another account."));
                resetUserData();
                return null;
            }

            // Update timestamp
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            usersFacade.edit(user);

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
            this.user = usersFacade.find(currentUser.getId());
            this.managingStaffProfile = managingStaffProfilesFacade.find(currentUser.getId());
        }
    }

    // account.xhtml

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
            if (usersFacade.isUsernameExists(user.getUsername())) {
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
            usersFacade.edit(user);

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

    // authentication-security.xhtml

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
            usersFacade.edit(user);

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

    public void initializeTOTP2FA() {
        try {
            // Generate new TOTP secret
            GoogleAuthenticatorKey key = gAuth.createCredentials();
            tempSecret = key.getKey();

            // Generate TOTP URL
            String encodedIssuer = URLEncoder.encode("HostelGuard™", StandardCharsets.UTF_8);
            String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            qrCodeUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                    encodedIssuer, encodedEmail, tempSecret, encodedIssuer);

            // Generate backup codes
            backupCodes = generateBackupCodes();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to initialize 2FA: " + e.getMessage()));
        }
    }

    private List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String code = String.format("%08d", random.nextInt(100000000));
            codes.add(code);
        }
        return codes;
    }

    @Transactional
    public void verifyAndEnableTOTP() {
        try {
            // Verify the provided TOTP code
            boolean isCodeValid = gAuth.authorize(tempSecret, Integer.parseInt(totpCode));

            if (!isCodeValid) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Invalid verification code. Please try again."));
                return;
            }

            // Check if this is the first MFA method
            List<MfaMethods> existingMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            boolean isFirstMethod = existingMethods.isEmpty();

            // Create new MFA method
            MfaMethods mfaMethod = new MfaMethods();
            mfaMethod.setUser(user);
            mfaMethod.setMethod(MfaMethods.MfaMethodType.TOTP);
            mfaMethod.setSecret(tempSecret);
            mfaMethod.setRecoveryCodes(gson.toJson(backupCodes));
            mfaMethod.setPrimary(isFirstMethod); // Set primary only if it's the first method
            mfaMethod.setEnabled(true);
            mfaMethod.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            mfaMethod.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Save MFA method
            mfaMethodsFacade.create(mfaMethod);

            // Update user's MFA status
            user.setIs_mfa_enable(true);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);

            // Clear sensitive data
            tempSecret = null;
            totpCode = null;
            qrCodeUrl = null;
            backupCodes = null;

            // Force refresh of user data
            user = usersFacade.find(user.getId());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Two-factor authentication has been enabled."));

        } catch (NumberFormatException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Invalid verification code format. Please enter only numbers."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to enable 2FA: " + e.getMessage()));
        }
    }

    @Transactional
    public void disableTOTP() {
        try {
            // Refresh the user entity
            user = usersFacade.find(user.getId());

            // Find and delete all TOTP methods
            List<MfaMethods> mfaMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            for (MfaMethods method : mfaMethods) {
                if (method.getMethod() == MfaMethods.MfaMethodType.TOTP) {
                    MfaMethods freshMethod = mfaMethodsFacade.find(method.getId());
                    if (freshMethod != null) {
                        mfaMethodsFacade.remove(freshMethod);
                    }
                }
            }

            // Update user's MFA status only if no methods remain
            List<MfaMethods> remainingMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            user.setIs_mfa_enable(!remainingMethods.isEmpty());
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);

            // Clear cached data
            sessions = null;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Two-factor authentication has been disabled."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to disable 2FA: " + e.getMessage()));
        }
    }

    public boolean getHasTOTPEnabled() {
        try {
            if (user == null) {
                return false;
            }
            // Get fresh user data
            Users freshUser = usersFacade.find(user.getId());
            if (!Boolean.TRUE.equals(freshUser.getIs_mfa_enable())) {
                return false;
            }
            List<MfaMethods> mfaMethods = mfaMethodsFacade.findMfaMethodsByUser(freshUser);
            return mfaMethods.stream()
                    .anyMatch(method -> method.getMethod() == MfaMethods.MfaMethodType.TOTP &&
                            Boolean.TRUE.equals(method.getEnabled()));
        } catch (Exception e) {
            return false;
        }
    }

    public String getPreferredMfaStatus() {
        List<MfaMethods> enabledMethods = mfaMethodsFacade.findMfaMethodsByUser(user)
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

            List<MfaMethods> allMethods = mfaMethodsFacade.findMfaMethodsByUser(user);

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
        return mfaMethodsFacade.findMfaMethodsByUser(user)
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getEnabled()))
                .toList();
    }

    public void initializeSMS2FA() {
        System.out.println("Enabling SMS 2FA...");
        // Generate mock verification code
        String mockCode = String.format("%06d", new Random().nextInt(999999));
        System.out.println("Mock SMS verification code generated: " + mockCode);

        // Generate backup codes
        backupCodes = generateBackupCodes();
    }

    public void initializeEmail2FA() {
        System.out.println("Enabling Email 2FA...");
        // Generate mock verification code
        String mockCode = String.format("%06d", new Random().nextInt(999999));
        System.out.println("Mock email verification code generated: " + mockCode);

        // Generate backup codes
        backupCodes = generateBackupCodes();
    }

    @Transactional
    public void verifyAndEnableSMSVerification() {
        try {
            // Verify the provided SMS code
            if (smsVerificationCode == null || smsVerificationCode.length() != 6) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Invalid verification code. Please enter a 6-digit code."));
                return;
            }

            // Check if this is the first MFA method
            List<MfaMethods> existingMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            boolean isFirstMethod = existingMethods.isEmpty();

            // Create new MFA method
            MfaMethods mfaMethod = new MfaMethods();
            mfaMethod.setUser(user);
            mfaMethod.setMethod(MfaMethods.MfaMethodType.SMS);
            mfaMethod.setEnabled(true);
            mfaMethod.setPrimary(isFirstMethod); // Set primary only if it's the first method
            mfaMethod.setRecoveryCodes(gson.toJson(backupCodes));
            mfaMethod.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            mfaMethod.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Save MFA method
            mfaMethodsFacade.create(mfaMethod);

            // Update user's MFA status
            user.setIs_mfa_enable(true);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);

            // Force refresh of user data
            user = usersFacade.find(user.getId());

            // Clear verification data but keep backup codes for display
            smsVerificationCode = null;
            hasSMSEnabled = true;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "SMS two-factor authentication has been enabled. Please save your recovery codes."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to enable SMS 2FA: " + e.getMessage()));
            smsVerificationCode = null;
            backupCodes = null;
        }
    }

    @Transactional
    public void verifyAndEnableEmailVerification() {
        try {
            // Verify the provided email code
            if (emailVerificationCode == null || emailVerificationCode.length() != 6) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "Invalid verification code. Please enter a 6-digit code."));
                return;
            }

            // Check if this is the first MFA method
            List<MfaMethods> existingMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            boolean isFirstMethod = existingMethods.isEmpty();

            // Create new MFA method
            MfaMethods mfaMethod = new MfaMethods();
            mfaMethod.setUser(user);
            mfaMethod.setMethod(MfaMethods.MfaMethodType.EMAIL);
            mfaMethod.setEnabled(true);
            mfaMethod.setPrimary(isFirstMethod); // Set primary only if it's the first method
            mfaMethod.setRecoveryCodes(gson.toJson(backupCodes));
            mfaMethod.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            mfaMethod.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // Save MFA method
            mfaMethodsFacade.create(mfaMethod);

            // Update user's MFA status
            user.setIs_mfa_enable(true);
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);

            // Clear verification data but keep backup codes for display
            emailVerificationCode = null;
            hasEmailEnabled = true;

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Email two-factor authentication has been enabled. Please save your recovery codes."));

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to enable Email 2FA: " + e.getMessage()));
            emailVerificationCode = null;
            backupCodes = null;
        }
    }

    @Transactional
    public void disableEmail2FA() {
        try {
            // Refresh the user entity
            user = usersFacade.find(user.getId());

            // Find and delete all Email MFA methods
            List<MfaMethods> mfaMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            for (MfaMethods method : mfaMethods) {
                if (method.getMethod() == MfaMethods.MfaMethodType.EMAIL) {
                    MfaMethods freshMethod = mfaMethodsFacade.find(method.getId());
                    if (freshMethod != null) {
                        mfaMethodsFacade.remove(freshMethod);
                    }
                }
            }

            // Update user's MFA status only if no methods remain
            List<MfaMethods> remainingMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            user.setIs_mfa_enable(!remainingMethods.isEmpty());
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);

            // Clear state
            hasEmailEnabled = false;
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
            // Refresh the user entity
            user = usersFacade.find(user.getId());

            // Find and delete all SMS MFA methods
            List<MfaMethods> mfaMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            for (MfaMethods method : mfaMethods) {
                if (method.getMethod() == MfaMethods.MfaMethodType.SMS) {
                    MfaMethods freshMethod = mfaMethodsFacade.find(method.getId());
                    if (freshMethod != null) {
                        mfaMethodsFacade.remove(freshMethod);
                    }
                }
            }

            // Update user's MFA status only if no methods remain
            List<MfaMethods> remainingMethods = mfaMethodsFacade.findMfaMethodsByUser(user);
            user.setIs_mfa_enable(!remainingMethods.isEmpty());
            user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            usersFacade.edit(user);

            // Clear state
            hasSMSEnabled = false;
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

    public boolean getHasEmailEnabled() {
        try {
            if (user == null) {
                return false;
            }
            // Get fresh user data
            Users freshUser = usersFacade.find(user.getId());
            if (!Boolean.TRUE.equals(freshUser.getIs_mfa_enable())) {
                return false;
            }
            List<MfaMethods> mfaMethods = mfaMethodsFacade.findMfaMethodsByUser(freshUser);
            return mfaMethods.stream()
                    .anyMatch(method -> method.getMethod() == MfaMethods.MfaMethodType.EMAIL &&
                            Boolean.TRUE.equals(method.getEnabled()));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean getHasSMSEnabled() {
        try {
            if (user == null) {
                return false;
            }
            // Get fresh user data
            Users freshUser = usersFacade.find(user.getId());
            if (!Boolean.TRUE.equals(freshUser.getIs_mfa_enable())) {
                return false;
            }
            List<MfaMethods> mfaMethods = mfaMethodsFacade.findMfaMethodsByUser(freshUser);
            return mfaMethods.stream()
                    .anyMatch(method -> method.getMethod() == MfaMethods.MfaMethodType.SMS &&
                            Boolean.TRUE.equals(method.getEnabled()));
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> getBackupCodes() {
        if (backupCodes != null && !backupCodes.isEmpty()) {
            return backupCodes;
        }

        MfaMethods primaryMethod = mfaMethodsFacade.findMfaMethodsByUser(user)
                .stream()
                .filter(m -> Boolean.TRUE.equals(m.getEnabled()) && Boolean.TRUE.equals(m.getPrimary()))
                .findFirst()
                .orElse(null);

        return primaryMethod != null && primaryMethod.getRecoveryCodes() != null ?
                gson.fromJson(primaryMethod.getRecoveryCodes(), new TypeToken<List<String>>() {
                }.getType()) :
                Collections.emptyList();
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
        return "/auth/sign-in.xhtml?faces-redirect=true";
    }

    // Getters & Setters
    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public ManagingStaffProfiles getManagingStaffProfile() {
        return managingStaffProfile;
    }

    public void setManagingStaffProfile(ManagingStaffProfiles managingStaffProfile) {
        this.managingStaffProfile = managingStaffProfile;
    }

    // Password Fields
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

    // Profile Image
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

    // MFA - TOTP
    public String getTotpCode() {
        return totpCode;
    }

    public void setTotpCode(String totpCode) {
        this.totpCode = totpCode;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    // MFA - General
    public MfaMethods.MfaMethodType getSelectedPrimaryMethod() {
        return selectedPrimaryMethod;
    }

    public void setSelectedPrimaryMethod(MfaMethods.MfaMethodType selectedPrimaryMethod) {
        this.selectedPrimaryMethod = selectedPrimaryMethod;
    }

    // MFA - Email/SMS
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

    // Session Management
    public UUID getCurrentSessionId() {
        return currentSessionId;
    }
}
