package org.example.hvvs.modules.auth.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.model.*;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.DigestUtils;
import org.example.hvvs.utils.ServiceResult;
import org.example.hvvs.utils.SessionCacheManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.io.InputStream;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Stateless
public class AuthServicesImpl implements AuthServices {
    @EJB
    private SessionCacheManager sessionCacheManager;

    @EJB
    private SessionService sessionService;

    @EJB
    private MfaMethodsFacade mfaMethodsFacade;

    @EJB
    private UsersFacade usersFacade;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();


    private final Gson gson = new Gson();

    @Override
    public ServiceResult<Users> signIn(String identifier, String password) {
        // Rate limiting check
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        String ipAddress = request.getRemoteAddr();
        String rateLimitKey = "login_attempts:" + ipAddress;

        // Check if blocked
        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            return ServiceResult.failure("RATE_LIMITED",
                    "Too many attempts. Please try again in 15 minutes.");
        }

        // Existing parameter check
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            return ServiceResult.failure("Email/username and password are required");
        }

        Users user;
        try {
            user = usersFacade.findByEmail(identifier);
            if (user == null) {
                user = usersFacade.findByUsername(identifier);
            }
        } catch (Exception e) {
            return ServiceResult.failure("DB_ERROR", "An error occurred when querying the user: " + e.getMessage());
        }
        if (user == null) {
            incrementFailedAttempt(rateLimitKey);
            return ServiceResult.failure("INVALID_CREDENTIALS", "Email/username or password is incorrect");
        }

        String passDigest = DigestUtils.sha256Digest(user.getSalt() + password);
        if (!user.getPassword().equals(passDigest)) {
            incrementFailedAttempt(rateLimitKey);
            return ServiceResult.failure("INVALID_CREDENTIALS", "Email/username or password is incorrect");
        }

        // Reset attempts on successful login
        sessionCacheManager.resetAttempts(rateLimitKey);
        return ServiceResult.success(user, "Sign-in successful");
    }

    private void incrementFailedAttempt(String rateLimitKey) {
        int attempts = sessionCacheManager.incrementAttempt(rateLimitKey);
        if (attempts >= 5) {
            sessionCacheManager.blockAccess(rateLimitKey, 15 * 60);
        }
    }

    public void registerSession(Users user) {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        HttpSession existingSession = (HttpSession) externalContext.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession newSession = (HttpSession) externalContext.getSession(true);

        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        UserSessions userSession = sessionService.createSession(
                user,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        newSession.setAttribute(CommonParam.SESSION_ID, userSession.getSession_id());
        newSession.setAttribute(CommonParam.SESSION_ROLE, user.getRole());
        newSession.setAttribute(CommonParam.SESSION_SELF, user);
        newSession.setAttribute(CommonParam.SESSION_EXPIRES_AT, userSession.getExpiresAt().getTime());
    }

    public String redirectBasedOnRole(Users user) {
        return switch (user.getRole()) {
            case RESIDENT -> "/resident/requests.xhtml?faces-redirect=true";
            case SECURITY_STAFF -> "/security/onboard-visitors.xhtml?faces-redirect=true";
            case MANAGING_STAFF -> "/admin/dashboard.xhtml?faces-redirect=true";
            case SUPER_ADMIN -> "/god/users.xhtml?faces-redirect=true";
        };
    }

    /**
     * Overloaded createMFA method that accepts a pre-generated secret and recovery codes.
     * This is used when the secret has been generated (and displayed) in the UI and then verified.
     */
    @Override
    public MfaMethods createMFA(Users user,
                                MfaMethods.MfaMethodType methodType,
                                String secret,
                                List<String> recoveryCodes) {

        // Re-fetch user to ensure it's up to date
        user = usersFacade.find(user.getId());

        // Check if there's any existing MFA method
        List<MfaMethods> existingMethods = mfaMethodsFacade.findEnabledMfaMethodsByUser(user);
        boolean isFirstMethod = existingMethods.isEmpty();

        // If it's the first method, mark user as having MFA
        if (isFirstMethod) {
            user.setIs_mfa_enable(true);
            user.setUpdatedAt(Timestamp.from(Instant.now()));
            usersFacade.edit(user);
        }

        // Build new MfaMethods entity
        MfaMethods method = new MfaMethods();
        method.setUser(user);
        method.setMethod(methodType);
        method.setSecret(secret);
        method.setRecoveryCodes(gson.toJson(recoveryCodes));
        method.setPrimary(isFirstMethod);
        method.setEnabled(true);
        method.setCreatedAt(Timestamp.from(Instant.now()));
        method.setUpdatedAt(Timestamp.from(Instant.now()));

        // Persist
        mfaMethodsFacade.create(method);

        return method;
    }

    @Override
    public void disableMFA(Users user, MfaMethods.MfaMethodType methodType) {
        // Re-fetch user to ensure we have the latest
        user = usersFacade.find(user.getId());

        // Find all methods of given type
        List<MfaMethods> userMethods = mfaMethodsFacade.findEnabledMfaMethodsByUser(user);
        for (MfaMethods m : userMethods) {
            if (m.getMethod() == methodType) {
                mfaMethodsFacade.remove(m);
            }
        }

        // Check how many remain after removal
        List<MfaMethods> remaining = mfaMethodsFacade.findEnabledMfaMethodsByUser(user);
        user.setIs_mfa_enable(!remaining.isEmpty());
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        usersFacade.edit(user);
    }

    @Override
    public String generateTotpSecret() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    @Override
    public boolean verifyTotpCode(String secret, int code) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        return gAuth.authorize(secret, code);
    }

    @Override
    public void sendSMSCode(MfaMethods method) {
        try {
            Users user = usersFacade.find(method.getUser().getId());

            // Generate 6-digit code
            String code = String.format("%06d", new Random().nextInt(999999));

            // Generate expiration time (10 minutes from now)
            long now = System.currentTimeMillis();
            long expiresAt = now + (10 * 60 * 1000); // 10 minutes in ms

            String codePlusExpiry = code + "~" + expiresAt;

            method.setUpdatedAt(Timestamp.from(Instant.ofEpochMilli(now)));
            method.setSecret(codePlusExpiry);
            mfaMethodsFacade.edit(method);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send sms code: " + e.getMessage());
        }
    }

    @Override
    public void sendEmailCode(MfaMethods method) {
        try {
            Users user = usersFacade.find(method.getUser().getId());
            String toAddress = user.getEmail();

            // Generate 6-digit code
            String code = String.format("%06d", new Random().nextInt(999999));

            // Generate expiration time (10 minutes from now)
            long now = System.currentTimeMillis();
            long expiresAt = now + (10 * 60 * 1000); // 10 minutes in ms

            String codePlusExpiry = code + "~" + expiresAt;

            method.setUpdatedAt(Timestamp.from(Instant.ofEpochMilli(now)));
            method.setSecret(codePlusExpiry);
            mfaMethodsFacade.edit(method);

            // Load properties from configuration file
            Properties props = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("META-INF/application.properties")) {
                if (input == null) {
                    throw new RuntimeException("Unable to find application.properties");
                }
                props.load(input);
            }

            String host = props.getProperty("mail.smtp.host");
            int port = Integer.parseInt(props.getProperty("mail.smtp.port"));
            String username = props.getProperty("mail.smtp.username");
            String password = props.getProperty("mail.smtp.password");
            String from = props.getProperty("mail.smtp.from");

            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.auth", props.getProperty("mail.smtp.auth"));
            mailProps.put("mail.smtp.starttls.enable", props.getProperty("mail.smtp.starttls.enable"));
            mailProps.put("mail.smtp.host", host);
            mailProps.put("mail.smtp.port", port);

            Session session = Session.getInstance(mailProps, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject("Your Verification Code");
            message.setText(String.format(
                    "Your HostelGuard verification code is: %s\n\n" +
                            "This code will expire in 10 minutes.",
                    code
            ));

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email code: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySMSCode(MfaMethods method, String userInput) {
        if (method.getSecret() == null) return false;

        String codePlusExpiry = method.getSecret();
        if (codePlusExpiry == null || codePlusExpiry.isBlank()) {
            return false;
        }

        // 2) Split it by the delimiter (e.g. "~")
        String[] parts = codePlusExpiry.split("~");
        if (parts.length != 2) {
            // The stored string is in an unexpected format
            return false;
        }

        String storedCode = parts[0];
        long storedExpiry;
        try {
            storedExpiry = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            // The expiry part isn't a valid number
            return false;
        }

        // 3) Check if the code has expired
        long now = System.currentTimeMillis();
        if (now > storedExpiry) {
            // Too late
            return false;
        }

        // 4) Compare the user's input to the stored code
        //    (Ensure that 'userInput' has the same leading zeros if needed.)
        if (!storedCode.equals(userInput)) {
            // Mismatch
            return false;
        }

        // 5) If we got here, the code matches and isn't expired
        //    Optional: You might want to clear it so it can't be reused
        method.setSecret(null);
        mfaMethodsFacade.edit(method);

        return true;
    }

    @Override
    public boolean verifyEmailCode(MfaMethods method, String userInput) {
        if (method.getSecret() == null) return false;

        String codePlusExpiry = method.getSecret();
        if (codePlusExpiry == null || codePlusExpiry.isBlank()) {
            return false;
        }

        // 2) Split it by the delimiter (e.g. "~")
        String[] parts = codePlusExpiry.split("~");
        if (parts.length != 2) {
            // The stored string is in an unexpected format
            return false;
        }

        String storedCode = parts[0];
        long storedExpiry;
        try {
            storedExpiry = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            // The expiry part isn't a valid number
            return false;
        }

        // 3) Check if the code has expired
        long now = System.currentTimeMillis();
        if (now > storedExpiry) {
            // Too late
            return false;
        }

        // 4) Compare the user's input to the stored code
        //    (Ensure that 'userInput' has the same leading zeros if needed.)
        if (!storedCode.equals(userInput)) {
            // Mismatch
            return false;
        }

        // 5) If we got here, the code matches and isn't expired
        //    Optional: You might want to clear it so it can't be reused
        method.setSecret(null);
        mfaMethodsFacade.edit(method);

        return true;
    }

    @Override
    public boolean verifyRecoveryCode(MfaMethods method, String codeUsed) {
        if (method.getRecoveryCodes() == null) {
            return false;
        }
        List<String> codes = gson.fromJson(method.getRecoveryCodes(), new TypeToken<List<String>>() {
        }.getType());
        boolean found = false;
        Iterator<String> it = codes.iterator();
        while (it.hasNext()) {
            String c = it.next();
            if (c.equalsIgnoreCase(codeUsed)) {
                found = true;
                it.remove();
                break;
            }
        }
        if (found) {
            method.setRecoveryCodes(gson.toJson(codes));
            method.setUpdatedAt(Timestamp.from(Instant.now()));
            mfaMethodsFacade.edit(method);
        }
        return found;
    }

    public List<String> generateRecoveryCodes() {
        List<String> codes = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            String code = String.format("%08d", random.nextInt(100000000));
            codes.add(code);
        }
        return codes;
    }
}
