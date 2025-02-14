package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.MfaMethodsFacade;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.SessionCacheManager;

import java.io.Serializable;

@Named("mfaController")
@ViewScoped
public class MFAController implements Serializable {

    @EJB
    private AuthServices authServices;

    @EJB
    private MfaMethodsFacade mfaMethodsFacade;

    @EJB
    private SessionCacheManager sessionCacheManager;

    private String code; // code the user inputs
    private boolean isRecoveryMode = false; // toggle between MFA and recovery code mode

    public String verifyMfa() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        String rateLimitKey = "login_attempts:" + request.getRemoteAddr();

        if (sessionCacheManager.isBlocked(rateLimitKey)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Too many attempts",
                            "Please try again in 15 minutes or use a recovery code"));
            return null;
        }

        try {
            // Retrieve the user from pre-auth session
            FacesContext ctx = FacesContext.getCurrentInstance();
            Users preAuthUser = (Users) ctx.getExternalContext()
                    .getSessionMap().get(CommonParam.PRE_AUTH_USER);

            if (preAuthUser == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Error", "Please login first!"));
                return "/auth/sign-in.xhtml?faces-redirect=true";
            }

            // Find the user's primary method from DB
            MfaMethods primaryMethod = mfaMethodsFacade.findPrimaryMfaMethodByUser(preAuthUser);

            if (primaryMethod == null) {
                ctx.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR, "Error", "There is no primary MFA method associated with the user."));
                return "/auth/sign-in.xhtml?faces-redirect=true";
            }

            boolean verificationSuccess = false;
            if (isRecoveryMode) {
                verificationSuccess = authServices.verifyRecoveryCode(primaryMethod, code);
                if (!verificationSuccess) {
                    ctx.addMessage(null, new FacesMessage(
                            FacesMessage.SEVERITY_ERROR, "Invalid Recovery Code",
                            "The recovery code you entered is incorrect or has already been used."));
                }
            } else {
                // Handle different MFA methods
                switch (primaryMethod.getMethod()) {
                    case TOTP:
                        int codeVal = Integer.parseInt(code);
                        verificationSuccess = authServices.verifyTotpCode(primaryMethod.getSecret(), codeVal);
                        break;
                    case SMS:
                        verificationSuccess = authServices.verifyEmailCode(primaryMethod, code);
                        break;
                    case EMAIL:
                        verificationSuccess = authServices.verifySMSCode(primaryMethod, code);
                        break;
                    default:
                        ctx.addMessage(null, new FacesMessage(
                                FacesMessage.SEVERITY_ERROR, "Error", 
                                "Unsupported MFA method"));
                        return null;
                }
            }

            if (!verificationSuccess) {
                int attempts = sessionCacheManager.incrementAttempt(rateLimitKey);
                if (attempts >= 5) {
                    sessionCacheManager.blockAccess(rateLimitKey, 15 * 60);
                }
                if (!isRecoveryMode) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Invalid code",
                                    "The verification code is incorrect"));
                }
                return null;
            }

            // Reset on success
            sessionCacheManager.resetAttempts(rateLimitKey);

            authServices.registerSession(preAuthUser);
            ctx.getExternalContext().getSessionMap().remove(CommonParam.PRE_AUTH_USER);

            return authServices.redirectBasedOnRole(preAuthUser);
        } catch (Exception e) {
            int attempts = sessionCacheManager.incrementAttempt(rateLimitKey);
            if (attempts >= 5) {
                sessionCacheManager.blockAccess(rateLimitKey, 15 * 60);
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "Verification failed: " + e.getMessage()));
            return null;
        }
    }

    // getters/setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean getIsRecoveryMode() {
        return isRecoveryMode;
    }

    public void setIsRecoveryMode(boolean isRecoveryMode) {
        this.isRecoveryMode = isRecoveryMode;
    }

    public int getRemainingAttempts() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        String ipAddress = request.getRemoteAddr();
        String rateLimitKey = "login_attempts:" + ipAddress;
        return sessionCacheManager.getRemainingAttempts(rateLimitKey);
    }

    public boolean isBlocked() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        String ipAddress = request.getRemoteAddr();
        String rateLimitKey = "login_attempts:" + ipAddress;
        return sessionCacheManager.isBlocked(rateLimitKey);
    }
}
