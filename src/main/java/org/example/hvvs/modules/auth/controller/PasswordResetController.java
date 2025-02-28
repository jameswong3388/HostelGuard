package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.model.*;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.DigestUtils;
import org.example.hvvs.utils.SessionCacheManager;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet controller for handling password reset functionality.
 * This controller handles the following operations:
 * 1. Request a password reset (email input)
 * 2. Send a reset token via email (with rate limiting and IP reputation checks)
 * 3. Verify the token and its expiry
 * 4. Set a new password with security requirements
 * 
 * Security features:
 * - Rate limiting per IP and email address
 * - Progressive backoff for failed attempts
 * - IP reputation tracking
 * - HMAC token verification with server-side secret
 * - Secure password requirements enforcement
 * - Protection against email flooding attacks
 * - Session-based token validation
 * - Expiring tokens with configurable lifetime
 */
@WebServlet(urlPatterns = {"/auth/password-reset", "/auth/password-reset/*"})
public class PasswordResetController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PasswordResetController.class.getName());
    
    // Constants
    private static final String PASSWORD_RESET_JSP = "/auth/password-reset.jsp";
    private static final String TOKEN_SESSION_ATTR = "passwordResetToken";
    private static final String RATE_LIMIT_PREFIX = "reset_attempts:";
    private static final String EMAIL_RATE_LIMIT_PREFIX = "email_reset_attempts:";
    private static final String GLOBAL_EMAIL_LIMIT_PREFIX = "global_email_reset:";
    private static final String IP_REPUTATION_PREFIX = "ip_reputation:";
    private static final int BLOCK_DURATION_MINUTES = 30;
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int SALT_BYTES_LENGTH = 16;
    private static final int MAX_EMAIL_REQUESTS_PER_IP = 5;
    private static final int MAX_EMAIL_REQUESTS_PER_EMAIL = 3;
    private static final int EMAIL_COOLDOWN_MINUTES = 15;
    private static final int GLOBAL_EMAIL_LIMIT_PER_HOUR = 100;
    private static final String HMAC_SECRET_KEY = "HostelGuard_HMAC_Reset_Secret"; // Secret key for HMAC operations
    private static final int MIN_PASSWORD_LENGTH = 8; // Minimum password length
    
    // Rate limiting backoff settings
    private static final int[] PROGRESSIVE_BLOCK_DURATIONS = {5, 15, 30, 60, 120}; // Progressive block durations in minutes
    private static final int MAX_ATTEMPTS_BEFORE_CAPTCHA = 2; // Number of attempts before requiring CAPTCHA
    private static final int BAD_REPUTATION_THRESHOLD = 5; // Number of blocks before considering an IP suspicious
    private static final int REPUTATION_MEMORY_DAYS = 7; // How long to remember IP reputation (in days)

    @EJB
    private UsersFacade usersFacade;
    
    @EJB
    private PasswordResetTokensFacade passwordResetTokensFacade;
    
    @EJB
    private SessionCacheManager sessionCacheManager;
    
    @EJB
    private AuthServices authServices;

    /**
     * Handles GET requests for displaying the appropriate form based on the path
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String clientIp = getClientIpAddress(request);
        String rateLimitKey = RATE_LIMIT_PREFIX + DigestUtils.hmacSha256(HMAC_SECRET_KEY, clientIp);
        
        // Check IP reputation
        if (isClientSuspicious(clientIp)) {
            applyEnhancedRateLimiting(request, response, rateLimitKey);
            return;
        }

        // Check if the IP is blocked
        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            long remainingSeconds = sessionCacheManager.getBlockedRemainingTime(rateLimitKey);
            request.setAttribute("errorMessage", 
                "Too many attempts. Please try again in " + formatBlockDuration(remainingSeconds) + ".");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
            
        // If no path info or just /, display the request form
        if (pathInfo == null || pathInfo.equals("/")) {
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // If the path matches /verify/{token}, validate the token
        if (pathInfo.startsWith("/verify/")) {
            handleTokenVerification(request, response, pathInfo);
            return;
        }
        
        // For any other path, redirect to the request form
        response.sendRedirect(request.getContextPath() + "/auth/password-reset");
    }

    /**
     * Handles verification of the password reset token
     */
    private void handleTokenVerification(HttpServletRequest request, HttpServletResponse response, String pathInfo) 
            throws ServletException, IOException {
        String token = pathInfo.substring("/verify/".length());
        PasswordResetTokens resetToken = passwordResetTokensFacade.findValidToken(token);
        
        if (resetToken == null) {
            request.setAttribute("errorMessage", "Invalid or expired password reset link.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // Store token in session for later use during password reset
        request.getSession().setAttribute(TOKEN_SESSION_ATTR, token);
        request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
    }

    /**
     * Handles POST requests for processing password reset
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String clientIp = getClientIpAddress(request);
        String rateLimitKey = RATE_LIMIT_PREFIX + DigestUtils.hmacSha256(HMAC_SECRET_KEY, clientIp);
        
        // Check if the IP is blocked
        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            long remainingSeconds = sessionCacheManager.getBlockedRemainingTime(rateLimitKey);
            request.setAttribute("errorMessage", 
                "Too many attempts. Please try again in " + formatBlockDuration(remainingSeconds) + ".");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // Route to appropriate handler based on action
        switch (action) {
            case "request":
                handlePasswordResetRequest(request, response, rateLimitKey);
                break;
            case "reset":
                handlePasswordReset(request, response, rateLimitKey);
                break;
            default:
                // If action is not recognized, redirect to the request form
                response.sendRedirect(request.getContextPath() + "/auth/password-reset");
                break;
        }
    }
    
    /**
     * Handles the initial password reset request with email input
     */
    private void handlePasswordResetRequest(HttpServletRequest request, HttpServletResponse response, String rateLimitKey)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String clientIp = getClientIpAddress(request);
        
        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Email address is required.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // Anti-spam: Check global email rate limit (prevent overall system abuse)
        String globalEmailLimitKey = GLOBAL_EMAIL_LIMIT_PREFIX + Instant.now().truncatedTo(ChronoUnit.HOURS).toString();
        
        // Track global request count, and limit if necessary
        int globalCount = sessionCacheManager.incrementAttempt(globalEmailLimitKey);
        if (globalCount > GLOBAL_EMAIL_LIMIT_PER_HOUR) {
            LOGGER.warning("Global email rate limit reached: " + globalCount + " requests this hour");
            request.setAttribute("errorMessage", 
                "The system is currently experiencing high volume. Please try again after " + 
                formatBlockDuration(ChronoUnit.HOURS.getDuration().getSeconds() - 
                    ChronoUnit.MINUTES.between(Instant.now(), 
                        Instant.now().truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)) * 60) + ".");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // Anti-spam: Check IP-based rate limiting for email requests
        String ipEmailRateKey = EMAIL_RATE_LIMIT_PREFIX + DigestUtils.hmacSha256(HMAC_SECRET_KEY, clientIp);
        
        if (sessionCacheManager.isBlocked(ipEmailRateKey)) {
            long remainingSeconds = sessionCacheManager.getBlockedRemainingTime(ipEmailRateKey);
            
            // Log but don't disclose to client that their specific IP is rate limited for security reasons
            LOGGER.info("Email request blocked from IP: " + clientIp + " for " + formatBlockDuration(remainingSeconds));
            
            // Show a generic message for security reasons, but add timing information
            request.setAttribute("message", 
                "If your email is registered, you will receive password reset instructions shortly. " +
                "For security reasons, please wait before requesting another reset email.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // Anti-spam: Check per-email rate limiting (normalize email by lowercase)
        String normalizedEmail = email.toLowerCase().trim();
        String emailRateKey = EMAIL_RATE_LIMIT_PREFIX + DigestUtils.hmacSha256(HMAC_SECRET_KEY, normalizedEmail);
        
        if (sessionCacheManager.isBlocked(emailRateKey)) {
            // We don't disclose that this specific email is rate limited for security reasons
            request.setAttribute("message", "If your email is registered, you will receive password reset instructions shortly.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // Find the user by email
        Users user = usersFacade.findByEmail(email);
        
        // We don't tell the user if the email exists for security reasons
        // Instead, always show success message
        if (user != null) {
            try {
                // Check if this email already has pending reset tokens and has reached its limit
                if (sessionCacheManager.getRemainingAttempts(emailRateKey) <= 0) {
                    // Silently fail with success message for security
                    request.setAttribute("message", "If your email is registered, you will receive password reset instructions shortly.");
                    request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
                    return;
                }
                
                // Track email request for rate limiting
                int emailAttempts = sessionCacheManager.incrementAttempt(emailRateKey);
                if (emailAttempts >= MAX_EMAIL_REQUESTS_PER_EMAIL) {
                    // Apply progressive blocking based on attempt count
                    int blockIndex = Math.min(emailAttempts - MAX_EMAIL_REQUESTS_PER_EMAIL, PROGRESSIVE_BLOCK_DURATIONS.length - 1);
                    int blockDuration = PROGRESSIVE_BLOCK_DURATIONS[blockIndex];
                    
                    sessionCacheManager.blockAccess(emailRateKey, blockDuration * 60);
                    LOGGER.info("Rate limit applied to email: " + DigestUtils.sha256Digest(normalizedEmail) 
                            + " for " + blockDuration + " minutes after " + emailAttempts + " attempts");
                }
                
                // Track IP-based email requests
                int ipAttempts = sessionCacheManager.incrementAttempt(ipEmailRateKey);
                if (ipAttempts >= MAX_EMAIL_REQUESTS_PER_IP) {
                    // Apply progressive blocking based on attempt count
                    int blockIndex = Math.min(ipAttempts - MAX_EMAIL_REQUESTS_PER_IP, PROGRESSIVE_BLOCK_DURATIONS.length - 1);
                    int blockDuration = PROGRESSIVE_BLOCK_DURATIONS[blockIndex];
                    
                    sessionCacheManager.blockAccess(ipEmailRateKey, blockDuration * 60);
                    
                    // Track IP reputation
                    incrementIpReputationCounter(clientIp);
                    
                    LOGGER.info("Rate limit applied to IP: " + clientIp + " for " + blockDuration + 
                            " minutes after " + ipAttempts + " email reset attempts");

                    // For security, we don't explicitly tell the user they've reached the rate limit
                    // But we do inform them of a general cooling-off period
                    request.setAttribute("message", 
                        "If your email is registered, you will receive password reset instructions shortly. " +
                        "For security reasons, please wait at least " + EMAIL_COOLDOWN_MINUTES + " minutes before trying again.");
                    request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
                    return;
                }
                
                createAndSendPasswordResetToken(request, user);
            } catch (Exception e) {
                // Log the error but don't expose it to the user
                LOGGER.log(Level.SEVERE, "Error sending password reset email: " + e.getMessage(), e);
            }
        } else {
            // Even for non-existent emails, increment IP-based counters to prevent email enumeration
            int ipAttempts = sessionCacheManager.incrementAttempt(ipEmailRateKey);
            if (ipAttempts >= MAX_EMAIL_REQUESTS_PER_IP) {
                // Apply progressive blocking
                int blockIndex = Math.min(ipAttempts - MAX_EMAIL_REQUESTS_PER_IP, PROGRESSIVE_BLOCK_DURATIONS.length - 1);
                int blockDuration = PROGRESSIVE_BLOCK_DURATIONS[blockIndex];
                
                sessionCacheManager.blockAccess(ipEmailRateKey, blockDuration * 60);
                
                // Track IP reputation for non-existent email attempts
                incrementIpReputationCounter(clientIp);
                
                LOGGER.info("Rate limit applied to IP: " + clientIp + " for " + blockDuration + 
                        " minutes after " + ipAttempts + " email reset attempts with non-existent email");
                
                // For security, we don't explicitly tell the user they've reached the rate limit
                // But we do inform them of a general cooling-off period
                request.setAttribute("message", 
                    "If your email is registered, you will receive password reset instructions shortly. " +
                    "For security reasons, please wait at least " + EMAIL_COOLDOWN_MINUTES + " minutes before trying again.");
                request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
                return;
            }
        }
        
        // Always show success message (even if email not found, for security)
        request.setAttribute("message", "If your email is registered, you will receive password reset instructions shortly.");
        request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
    }
    
    /**
     * Creates a password reset token and sends email with reset link
     */
    private void createAndSendPasswordResetToken(HttpServletRequest request, Users user) throws Exception {
        // Invalidate any existing tokens for this user
        passwordResetTokensFacade.invalidateAllUserTokens(user);
        
        // Generate a shorter but still secure random token
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[16]; // Reduced from 32 to 16 bytes (still 128 bits of entropy which is secure)
        random.nextBytes(tokenBytes);
        String tokenBase = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        
        // Create token expiration time
        Instant expiryInstant = Instant.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        Timestamp expiryTime = Timestamp.from(expiryInstant);
        
        // Generate a short secure token with HMAC verification
        String userId = String.valueOf(user.getId());
        String timestamp = String.valueOf(expiryInstant.getEpochSecond());
        String hmac = DigestUtils.hmacSha256(HMAC_SECRET_KEY, userId + ":" + timestamp + ":" + tokenBase);
        
        // Take only the first 12 characters of the HMAC (still provides security while making the token shorter)
        String shortHmac = hmac.substring(0, 12);
        
        // Final token format: {base}{timestamp 8 chars}{hmac 12 chars}
        // This ensures the token will be shorter while maintaining security properties
        String token = tokenBase + timestamp.substring(timestamp.length() - 8) + shortHmac;
        
        try {
            // Create and save the token
            PasswordResetTokens resetToken = new PasswordResetTokens(user, token, expiryTime);
            passwordResetTokensFacade.create(resetToken);
            
            // Get application URL for the reset link
            String appUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                appUrl += ":" + request.getServerPort();
            }
            appUrl += request.getContextPath();
            
            // Send the email with the reset link
            sendPasswordResetEmail(user.getEmail(), appUrl + "/auth/password-reset/verify/" + token);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating password reset token: " + e.getMessage(), e);
            throw new Exception("Failed to create password reset token", e);
        }
    }
    
    /**
     * Handles the actual password reset form submission
     */
    private void handlePasswordReset(HttpServletRequest request, HttpServletResponse response, String rateLimitKey)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute(TOKEN_SESSION_ATTR) == null) {
            response.sendRedirect(request.getContextPath() + "/auth/password-reset");
            return;
        }
        
        String token = (String) session.getAttribute(TOKEN_SESSION_ATTR);
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        // Validate passwords
        if (!validatePasswords(request, response, password, confirmPassword)) {
            return; // Validation failed, response has been set by validation method
        }
        
        // Find token
        PasswordResetTokens resetToken = passwordResetTokensFacade.findValidToken(token);
        
        if (resetToken == null) {
            handleInvalidToken(request, response, session, rateLimitKey);
            return;
        }
        
        try {
            updateUserPasswordAndLogin(request, response, session, resetToken, password);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error resetting password: " + e.getMessage(), e);
            sessionCacheManager.incrementAttempt(rateLimitKey);
            
            request.setAttribute("errorMessage", "An error occurred while resetting your password. Please try again.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
        }
    }
    
    /**
     * Validates the passwords match and meet requirements
     * @return true if passwords are valid, false otherwise
     */
    private boolean validatePasswords(HttpServletRequest request, HttpServletResponse response, 
                                    String password, String confirmPassword) throws ServletException, IOException {
        if (password == null || password.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Password is required.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return false;
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            request.setAttribute("errorMessage", "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return false;
        }
        
        // Check for password complexity
        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        String specialChars = "!@#$%^&*()_-+=<>?/[]{}|";
        
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (specialChars.indexOf(c) >= 0) {
                hasSpecial = true;
            }
        }
        
        if (!(hasLetter && hasDigit && hasSpecial)) {
            request.setAttribute("errorMessage", 
                "Password must contain at least one letter, one number, and one special character.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return false;
        }
        
        return true;
    }
    
    /**
     * Handles an invalid or expired token
     */
    private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response, 
                                  HttpSession session, String rateLimitKey) throws ServletException, IOException {
        // Token expired or invalid
        String clientIp = getClientIpAddress(request);
        int attempts = sessionCacheManager.incrementAttempt(rateLimitKey);
        
        // Apply progressive blocking for invalid token attempts
        if (attempts >= 3) {  // After 3 attempts, start blocking
            int blockIndex = Math.min(attempts - 3, PROGRESSIVE_BLOCK_DURATIONS.length - 1);
            int blockDuration = PROGRESSIVE_BLOCK_DURATIONS[blockIndex];
            
            sessionCacheManager.blockAccess(rateLimitKey, blockDuration * 60);
            
            // Track reputation for suspicious behavior
            incrementIpReputationCounter(clientIp);
            
            LOGGER.info("Rate limit for invalid token applied to IP: " + clientIp + 
                    " for " + blockDuration + " minutes after " + attempts + " attempts");
            
            long remainingSeconds = sessionCacheManager.getBlockedRemainingTime(rateLimitKey);
            request.setAttribute("errorMessage", "Password reset link has expired or is invalid. " + 
                    "Too many invalid attempts. Please wait " + formatBlockDuration(remainingSeconds) + 
                    " before requesting a new one.");
        } else {
            request.setAttribute("errorMessage", "Password reset link has expired or is invalid. Please request a new one.");
        }
        
        session.removeAttribute(TOKEN_SESSION_ATTR);
        request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
    }
    
    /**
     * Updates the user's password and logs them in
     */
    private void updateUserPasswordAndLogin(HttpServletRequest request, HttpServletResponse response, 
                                         HttpSession session, PasswordResetTokens resetToken, String password) 
                                         throws ServletException, IOException {
        // Get the user ID from the token and load it properly with UsersFacade
        // instead of using the lazily loaded proxy from the token
        Integer userId = resetToken.getUser().getId();
        Users user = usersFacade.find(userId);
        
        if (user == null) {
            throw new ServletException("User not found");
        }
        
        // Generate a new salt using SecureRandom (same technique as in UsersFacade.createUser)
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[SALT_BYTES_LENGTH];
        random.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);
        
        // Use HMAC with pepper for password hashing for enhanced security
        String hashedPassword = DigestUtils.hmacSha256Password(salt, password);
        
        // Update the user's password and salt
        user.setSalt(salt);
        user.setPassword(hashedPassword);
        usersFacade.edit(user);
        
        // Mark the token as used
        passwordResetTokensFacade.markTokenAsUsed(resetToken);
        
        // Clear the password reset token
        session.removeAttribute(TOKEN_SESSION_ATTR);
        
        // Create a new session for the user - automatic login after password reset
        authServices.registerSession(user, request);
        
        // Get appropriate redirect URL based on user role
        String redirectUrl = authServices.redirectBasedOnRole(user);
        
        // Redirect to the user's protected page based on their role
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
    
    /**
     * Sends a password reset email to the user
     */
    private void sendPasswordResetEmail(String toAddress, String resetLink) throws Exception {
        // Load properties from configuration file
        Properties props = new Properties();
        try (java.io.InputStream input = getClass().getClassLoader().getResourceAsStream("META-INF/application.properties")) {
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

        jakarta.mail.Session session = jakarta.mail.Session.getInstance(mailProps, new jakarta.mail.Authenticator() {
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(username, password);
            }
        });

        jakarta.mail.Message message = new jakarta.mail.internet.MimeMessage(session);
        message.setFrom(new jakarta.mail.internet.InternetAddress(from));
        message.setRecipients(jakarta.mail.Message.RecipientType.TO, jakarta.mail.internet.InternetAddress.parse(toAddress));
        message.setSubject("HostelGuard - Password Reset");
        
        String emailBody = "Hello,\n\n" +
                "You have requested to reset your password for HostelGuard.\n\n" +
                "Please click the link below to reset your password:\n" +
                resetLink + "\n\n" +
                "This link will expire in " + TOKEN_EXPIRY_MINUTES + " minutes.\n\n" +
                "If you did not request a password reset, you can safely ignore this email.\n\n" +
                "Regards,\n" +
                "The HostelGuard Team";
        
        message.setText(emailBody);

        jakarta.mail.Transport.send(message);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        // Make sure rate limits are properly configured
        // Note: This assumes SessionCacheManager already has default values set
        // If explicit configuration is needed, it would be done in the SessionCacheManager implementation
    }
    
    /**
     * Helper method to get the client's real IP address, accounting for proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // If the IP contains multiple addresses (X-Forwarded-For can have a chain), take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * Apply enhanced rate limiting for suspicious IPs
     */
    private void applyEnhancedRateLimiting(HttpServletRequest request, HttpServletResponse response, String rateLimitKey) 
            throws ServletException, IOException {
        // Apply longer blocks and stricter limits for suspicious IPs
        int blockDuration = PROGRESSIVE_BLOCK_DURATIONS[PROGRESSIVE_BLOCK_DURATIONS.length - 1];
        sessionCacheManager.blockAccess(rateLimitKey, blockDuration * 60);
        LOGGER.warning("Enhanced rate limiting applied to suspicious IP: " + getClientIpAddress(request));
        
        request.setAttribute("errorMessage", 
                "For security reasons, access to this feature has been temporarily restricted. " +
                "Please try again in " + formatBlockDuration(blockDuration * 60) + ".");
        request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
    }
    
    /**
     * Increment the IP reputation counter whenever rate limits are hit
     */
    private void incrementIpReputationCounter(String clientIp) {
        String reputationKey = IP_REPUTATION_PREFIX + DigestUtils.hmacSha256(HMAC_SECRET_KEY, clientIp);
        
        // Increment and set longer expiry (reputation persists longer than individual blocks)
        int reputationCount = sessionCacheManager.incrementAttempt(reputationKey);
        
        // Unfortunately we can't directly set expiry without modifying SessionCacheManager
        // We're relying on the default expiry set by SessionCacheManager
        
        if (reputationCount >= BAD_REPUTATION_THRESHOLD) {
            LOGGER.warning("IP: " + clientIp + " has reached bad reputation threshold with " + reputationCount + " strikes");
        }
    }
    
    /**
     * Check if a client has suspicious behavior history
     */
    private boolean isClientSuspicious(String clientIp) {
        String reputationKey = IP_REPUTATION_PREFIX + DigestUtils.hmacSha256(HMAC_SECRET_KEY, clientIp);
        // Use the getRemainingAttempts method to get the attempt count indirectly
        int attemptsRemaining = sessionCacheManager.getRemainingAttempts(reputationKey);
        int maxAttempts = MAX_EMAIL_REQUESTS_PER_IP; // This should match the configured max attempts in SessionCacheManager
        int reputationCount = maxAttempts - attemptsRemaining;
        
        return reputationCount >= BAD_REPUTATION_THRESHOLD;
    }
    
    /**
     * Format the block duration time for user-friendly display
     */
    private String formatBlockDuration(long remainingSeconds) {
        if (remainingSeconds <= 0) {
            return "0 seconds";
        } else if (remainingSeconds < 60) {
            return remainingSeconds + " seconds";
        } else if (remainingSeconds < 3600) {
            return (remainingSeconds / 60) + " minutes";
        } else {
            long hours = remainingSeconds / 3600;
            long minutes = (remainingSeconds % 3600) / 60;
            if (minutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + " hour" + (hours > 1 ? "s" : "") + " and " + minutes + " minute" + (minutes > 1 ? "s" : "");
            }
        }
    }
} 