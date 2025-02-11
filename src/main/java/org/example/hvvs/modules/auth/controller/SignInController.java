package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.inject.Inject;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.ServiceResult;
import org.example.hvvs.utils.SessionCacheManager;

import java.io.Serializable;

@Named("signInController")
@RequestScoped
public class SignInController implements Serializable {

    @EJB
    private AuthServices authServices;

    @Inject
    private SessionCacheManager sessionCacheManager;

    private String identifier; // Can be either email or username
    private String password;

    public String signIn() {
        try {
            ServiceResult<Users> serviceResult = authServices.signIn(identifier, password);

            if (!serviceResult.isSuccess()) {
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", serviceResult.getMessage()));
                return "/auth/sign-in.xhtml?faces-redirect=true";
            }

            Users user = serviceResult.getData();

            if (user.getIs_mfa_enable()) {
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                        .put(CommonParam.PRE_AUTH_USER, user);

                password = null;

                return "/auth/mfa.xhtml?faces-redirect=true";
            }

            authServices.registerSession(user);

            // Now redirect or navigate based on the user's role
            return authServices.redirectBasedOnRole(user);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Login failed: " + e.getMessage()));
            return "/auth/sign-in.xhtml?faces-redirect=true";
        } finally {
            // Ensure password String is cleared from memory
            password = null;
        }
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

    // Getters and Setters
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
