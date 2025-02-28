package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.hvvs.model.Notifications;
import org.example.hvvs.model.UsersFacade;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.common.service.NotificationService;
import org.example.hvvs.utils.SessionCacheManager;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@WebServlet("/auth/signUp")
public class SignUpController extends HttpServlet {

    @EJB
    private UsersFacade usersFacade;

    @EJB
    private NotificationService notificationService;

    @EJB
    private SessionCacheManager sessionCacheManager;

    private static final String SIGN_UP_JSP = "/auth/sign-up.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Simply forward to the sign-up form
        getServletContext()
            .getRequestDispatcher(SIGN_UP_JSP)
            .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get all form parameters
        String username = request.getParameter("username");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String phoneNumber = request.getParameter("phoneNumber");
        String identityNumber = request.getParameter("identityNumber");
        String address = request.getParameter("address");
        String genderStr = request.getParameter("gender");
        String roleStr = request.getParameter("role");
        String additionalInfo = request.getParameter("additionalInfo");

        // Validate inputs
        if (username == null || username.trim().isEmpty() ||
            firstName == null || firstName.trim().isEmpty() ||
            lastName == null || lastName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phoneNumber == null || phoneNumber.trim().isEmpty() ||
            identityNumber == null || identityNumber.trim().isEmpty() ||
            roleStr == null || roleStr.trim().isEmpty()) {
            
            request.setAttribute("errorMessage", "All required fields must be filled out.");
            getServletContext()
                .getRequestDispatcher(SIGN_UP_JSP)
                .forward(request, response);
            return;
        }

        // Check if username or email already exists
        if (usersFacade.findByUsername(username) != null) {
            request.setAttribute("errorMessage", "Username already exists. Please choose another.");
            getServletContext()
                .getRequestDispatcher(SIGN_UP_JSP)
                .forward(request, response);
            return;
        }

        if (usersFacade.findByEmail(email) != null) {
            request.setAttribute("errorMessage", "Email already exists. Please use another email address.");
            getServletContext()
                .getRequestDispatcher(SIGN_UP_JSP)
                .forward(request, response);
            return;
        }

        // Convert gender and role strings to enum values
        Users.Gender gender = null;
        if (genderStr != null && !genderStr.trim().isEmpty()) {
            try {
                gender = Users.Gender.valueOf(genderStr);
            } catch (IllegalArgumentException e) {
                request.setAttribute("errorMessage", "Invalid gender selection.");
                getServletContext()
                    .getRequestDispatcher(SIGN_UP_JSP)
                    .forward(request, response);
                return;
            }
        }

        Users.Role role = null;
        try {
            role = Users.Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", "Invalid role selection.");
            getServletContext()
                .getRequestDispatcher(SIGN_UP_JSP)
                .forward(request, response);
            return;
        }

        // Find all admin users to notify
        List<Users> admins = usersFacade.findByRole(Users.Role.MANAGING_STAFF);

        if (admins.isEmpty()) {
            request.setAttribute("errorMessage", "No administrators found in the system. Please contact support.");
            getServletContext()
                .getRequestDispatcher(SIGN_UP_JSP)
                .forward(request, response);
            return;
        }

        // Send notification to all admins
        for (Users admin : admins) {
            String title = "New Account Request: " + firstName + " " + lastName;
            
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("A new account request has been submitted:\n\n");
            messageBuilder.append("Username: ").append(username).append("\n");
            messageBuilder.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
            messageBuilder.append("Email: ").append(email).append("\n");
            messageBuilder.append("Phone: ").append(phoneNumber).append("\n");
            messageBuilder.append("Identity Number: ").append(identityNumber).append("\n");
            messageBuilder.append("Requested Role: ").append(role).append("\n");
            
            if (address != null && !address.trim().isEmpty()) {
                messageBuilder.append("Address: ").append(address).append("\n");
            }
            
            if (gender != null) {
                messageBuilder.append("Gender: ").append(gender).append("\n");
            }
            
            if (additionalInfo != null && !additionalInfo.trim().isEmpty()) {
                messageBuilder.append("\nAdditional Information:\n").append(additionalInfo);
            }
            
            String message = messageBuilder.toString();
            
            // Create the notification for each admin
            notificationService.createNotification(
                admin,
                Notifications.NotificationType.SYSTEM_UPDATE,
                title,
                message,
                null,
                null
            );
        }

        // Set success message
        request.setAttribute("successMessage", 
            "Your account request has been submitted successfully. " +
            "An administrator will review your request and contact you via email once your account is ready.");

        // Forward back to the sign-up page with success message
        getServletContext()
            .getRequestDispatcher(SIGN_UP_JSP)
            .forward(request, response);
    }
} 