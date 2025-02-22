package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.MfaMethodsFacade;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.ServiceResult;
import org.example.hvvs.utils.SessionCacheManager;

import java.io.IOException;

@WebServlet("/auth/signIn")
public class SignInController extends HttpServlet {

    @EJB
    private AuthServices authServices;

    @EJB
    private SessionCacheManager sessionCacheManager;

    @EJB
    private MfaMethodsFacade mfaMethodsFacade;

    private static final String SIGN_IN_JSP = "/auth/sign-in.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Typically just forward to the sign-in form
        forwardToSignInForm(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        // Identify user by IP for rate-limit checks
        String ipAddress = request.getRemoteAddr();
        String rateLimitKey = "login_attempts:" + ipAddress;

        // Check if this IP is currently blocked
        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            request.setAttribute("errorMessage", "Account is temporarily locked. Please try again in 15 minutes.");
            forwardToSignInForm(request, response);
            return;
        }

        // Attempt to sign in
        ServiceResult<Users> serviceResult;
        try {
            serviceResult = authServices.signIn(identifier, password, request);

            if (!serviceResult.isSuccess()) {
                request.setAttribute("errorMessage", serviceResult.getMessage());
                forwardToSignInForm(request, response);
                return;
            }
        } catch (Exception e) {
            // Handle any unexpected exceptions from the service/EJB layer
            request.setAttribute("errorMessage", "Login failed: " + e.getMessage());
            incrementAttemptsAndForward(rateLimitKey, request, response);
            return;
        } finally {
            // Clear sensitive data
            password = null;
        }
        
        // Sign-in succeeded
        Users user = serviceResult.getData();

        // Check if MFA is enabled
        if (user.getIs_mfa_enable()) {
            // Store the user in session so that we can verify in the MFA flow
            request.getSession().setAttribute(CommonParam.PRE_AUTH_USER, user);

            // Find the user's primary MFA method
            MfaMethods primaryMethod = mfaMethodsFacade.findPrimaryMfaMethodByUser(user);
            if (primaryMethod == null) {
                request.setAttribute("errorMessage", "No primary MFA method associated with the user.");
                forwardToSignInForm(request, response);
                return;
            }

            // Trigger the MFA step (e.g., send code via email/sms, etc.)
            switch (primaryMethod.getMethod()) {
                case EMAIL:
                    authServices.sendEmailCode(primaryMethod);
                    break;
                case SMS:
                    authServices.sendSMSCode(primaryMethod);
                    break;
                case TOTP:
                    // TOTP typically checked via authenticator app
                    break;
            }

            // Redirect to an MFA verification page (JSP or another Servlet)
            response.sendRedirect(request.getContextPath() + "/auth/mfa");
            return;
        }

        // If MFA is not enabled, finalize login
        authServices.registerSession(user, request);

        // Redirect based on user role
        String redirectUrl = authServices.redirectBasedOnRole(user);
        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    /**
     * Increments the login attempt count and forwards the user back to the sign-in form.
     */
    private void incrementAttemptsAndForward(String rateLimitKey, HttpServletRequest request,
                                             HttpServletResponse response) throws ServletException, IOException {
        sessionCacheManager.incrementAttempt(rateLimitKey);
        forwardToSignInForm(request, response);
    }

    /**
     * Forwards to the sign-in JSP, adding lockout/attempts info as request attributes.
     */
    private void forwardToSignInForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to JSP
        getServletContext()
                .getRequestDispatcher(SIGN_IN_JSP)
                .forward(request, response);
    }
}
