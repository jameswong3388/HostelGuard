package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.model.User;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.ServiceResult;

import java.io.Serializable;

@Named("signInController")
@RequestScoped
public class SignInController implements Serializable {

    @EJB
    private AuthServices authServices;

    private String identifier; // Can be either email or username
    private String password;
    private boolean rememberMe;

    public String login() {
        try {
            ServiceResult<User> serviceResult = authServices.signIn(identifier, password, rememberMe);

            if (!serviceResult.isSuccess()) {
                // Failed authentication: show the error message
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", serviceResult.getMessage()));
                return null;
            }

            // If success, retrieve the authenticated user
            User user = serviceResult.getData();

            // Store in session
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            session.setAttribute(CommonParam.SESSION_ROLE, user.getRole());
            session.setAttribute(CommonParam.SESSION_SELF, user);

            if (Boolean.TRUE.equals(rememberMe)) {
                String autoLoginDigest = authServices.getAutoLoginCookieValue(user.getId());
                
                if (autoLoginDigest != null) {
                    Cookie cookie = new Cookie(CommonParam.COOKIE_AUTO_LOGIN, autoLoginDigest);
                    cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                    cookie.setPath("/");
                    HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance()
                            .getExternalContext().getResponse();
                    response.addCookie(cookie);
                }
            }

            // Now redirect or navigate based on the user's role
            return switch (user.getRole()) {
                case CommonParam.SESSION_ROLE_RESIDENT -> "resident/requests";
                case CommonParam.SESSION_ROLE_SECURITY_STAFF -> "security/visits";
                case CommonParam.SESSION_ROLE_MANAGING_STAFF -> "admin/dashboard";
                default -> {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Error", "Invalid user role: " + user.getRole()));
                    yield null;
                }
            };
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "An error occurred during login: " + e.getMessage()));
            return null;
        }
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

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
