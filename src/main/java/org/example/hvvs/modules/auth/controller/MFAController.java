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
import org.example.hvvs.utils.SessionCacheManager;

import java.io.IOException;

@WebServlet("/auth/mfa")
public class MFAController extends HttpServlet {

    @EJB
    private AuthServices authServices;

    @EJB
    private MfaMethodsFacade mfaMethodsFacade;

    @EJB
    private SessionCacheManager sessionCacheManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Check pre-auth session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(CommonParam.PRE_AUTH_USER) == null) {
            response.sendRedirect(request.getContextPath() + "/auth/sign-in.jsp");
            return;
        }

        // Add remaining attempts to request
        String rateLimitKey = "login_attempts:" + request.getRemoteAddr();
        request.setAttribute("remainingAttempts", sessionCacheManager.getRemainingAttempts(rateLimitKey));
        request.setAttribute("isBlocked", sessionCacheManager.isBlocked(rateLimitKey));

        request.getRequestDispatcher("/auth/mfa.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");
        boolean isRecoveryMode = "true".equals(request.getParameter("isRecoveryMode"));
        String rateLimitKey = "login_attempts:" + request.getRemoteAddr();

        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            request.setAttribute("errorMessage", "Too many attempts. Please try again in 15 minutes.");
            doGet(request, response);
            return;
        }

        try {
            HttpSession session = request.getSession();
            Users preAuthUser = (Users) session.getAttribute(CommonParam.PRE_AUTH_USER);

            if (preAuthUser == null) {
                response.sendRedirect(request.getContextPath() + "/auth/sign-in");
                return;
            }

            MfaMethods primaryMethod = mfaMethodsFacade.findPrimaryMfaMethodByUser(preAuthUser);
            if (primaryMethod == null) {
                session.removeAttribute(CommonParam.PRE_AUTH_USER);
                response.sendRedirect(request.getContextPath() + "/auth/sign-in");
                return;
            }

            boolean verificationSuccess = false;
            if (isRecoveryMode) {
                verificationSuccess = authServices.verifyRecoveryCode(primaryMethod, code);
            } else {
                switch (primaryMethod.getMethod()) {
                    case TOTP:
                        verificationSuccess = authServices.verifyTotpCode(primaryMethod.getSecret(), Integer.parseInt(code));
                        break;
                    case SMS:
                        verificationSuccess = authServices.verifySMSCode(primaryMethod, code);
                        break;
                    case EMAIL:
                        verificationSuccess = authServices.verifyEmailCode(primaryMethod, code);
                        break;
                    default:
                        throw new ServletException("Unsupported MFA method");
                }
            }

            if (!verificationSuccess) {
                int attempts = sessionCacheManager.incrementAttempt(rateLimitKey);
                if (attempts >= 5) {
                    sessionCacheManager.blockAccess(rateLimitKey, 15 * 60);
                }
                request.setAttribute("errorMessage", isRecoveryMode ?
                        "Invalid recovery code" : "Invalid verification code");
                doGet(request, response);
                return;
            }

            // Successful verification
            sessionCacheManager.resetAttempts(rateLimitKey);
            authServices.registerSession(preAuthUser, request);

            String redirectUrl = authServices.redirectBasedOnRole(preAuthUser);
            response.sendRedirect(request.getContextPath() + redirectUrl);
        } catch (Exception e) {
            sessionCacheManager.incrementAttempt(rateLimitKey);
            request.setAttribute("errorMessage", "Verification failed: " + e.getMessage());
            doGet(request, response);
        }
    }
}