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
 * 2. Send a reset token via email
 * 3. Verify the token
 * 4. Set a new password
 */
@WebServlet(urlPatterns = {"/auth/password-reset", "/auth/password-reset/*"})
public class PasswordResetController extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PasswordResetController.class.getName());
    
    // Constants
    private static final String PASSWORD_RESET_JSP = "/auth/password-reset.jsp";
    private static final String TOKEN_SESSION_ATTR = "passwordResetToken";
    private static final String RATE_LIMIT_PREFIX = "reset_attempts:";
    private static final int BLOCK_DURATION_MINUTES = 30;
    private static final int TOKEN_EXPIRY_MINUTES = 15;
    private static final int SALT_BYTES_LENGTH = 16;

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
        String rateLimitKey = RATE_LIMIT_PREFIX + request.getRemoteAddr();

        // Check if the IP is blocked
        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            request.setAttribute("errorMessage", "Too many attempts. Please try again in " + BLOCK_DURATION_MINUTES + " minutes.");
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
        String rateLimitKey = RATE_LIMIT_PREFIX + request.getRemoteAddr();
        
        // Check if the IP is blocked
        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            request.setAttribute("errorMessage", "Too many attempts. Please try again in " + BLOCK_DURATION_MINUTES + " minutes.");
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
        
        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Email address is required.");
            request.getRequestDispatcher(PASSWORD_RESET_JSP).forward(request, response);
            return;
        }
        
        // Find the user by email
        Users user = usersFacade.findByEmail(email);
        
        // We don't tell the user if the email exists for security reasons
        // Instead, always show success message
        if (user != null) {
            try {
                createAndSendPasswordResetToken(request, user);
            } catch (Exception e) {
                // Log the error but don't expose it to the user
                LOGGER.log(Level.SEVERE, "Error sending password reset email: " + e.getMessage(), e);
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
        
        // Generate a new token
        String token = UUID.randomUUID().toString();
        
        // Create token expiration (15 minutes from now)
        Timestamp expiryTime = Timestamp.from(Instant.now().plus(TOKEN_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        
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
        sessionCacheManager.incrementAttempt(rateLimitKey);
        if (sessionCacheManager.getRemainingAttempts(rateLimitKey) <= 0) {
            sessionCacheManager.blockAccess(rateLimitKey, BLOCK_DURATION_MINUTES * 60); // Block for configured minutes
        }
        
        session.removeAttribute(TOKEN_SESSION_ATTR);
        request.setAttribute("errorMessage", "Password reset link has expired or is invalid. Please request a new one.");
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
        
        // Hash the new password with the generated salt
        String hashedPassword = DigestUtils.sha256Digest(salt + password);
        
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
} 