package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.ServiceResult;

import java.io.Serializable;

@Named("signInController")
@RequestScoped
public class SignInController implements Serializable {

    @EJB
    private AuthServices authServices;

    @EJB
    private SessionService sessionService;

    private String identifier; // Can be either email or username
    private String password;

    public String login() {
        try {
            ServiceResult<Users> serviceResult = authServices.signIn(identifier, password);

            if (!serviceResult.isSuccess()) {
                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", serviceResult.getMessage()));
                return "/auth.xhtml?faces-redirect=true";
            }

            Users user = serviceResult.getData();
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();

            // Invalidate existing session
            HttpSession existingSession = (HttpSession) externalContext.getSession(false);
            if (existingSession != null) {
                existingSession.invalidate();
            }

            // Create new session
            HttpSession newSession = (HttpSession) externalContext.getSession(true);

            HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
            UserSessions userSession = sessionService.createSession(
                    user,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent")
            );

            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            session.setAttribute(CommonParam.SESSION_ID, userSession.getSession_id());
            session.setAttribute(CommonParam.SESSION_ROLE, user.getRole());
            session.setAttribute(CommonParam.SESSION_SELF, user);
            newSession.setAttribute(CommonParam.SESSION_EXPIRES_AT,
                    userSession.getExpiresAt().getTime());

            // Now redirect or navigate based on the user's role
            return switch (user.getRole()) {
                case CommonParam.SESSION_ROLE_RESIDENT -> "/resident/requests.xhtml?faces-redirect=true";
                case CommonParam.SESSION_ROLE_SECURITY_STAFF -> "/security/onboard-visitors.xhtml?faces-redirect=true";
                case CommonParam.SESSION_ROLE_MANAGING_STAFF -> "/admin/dashboard.xhtml?faces-redirect=true";
                default -> throw new IllegalStateException("Invalid role: " + user.getRole());
            };
        } catch (Exception e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Login failed: " + e.getMessage()));
            return "/auth.xhtml?faces-redirect=true";
        } finally {
            // Ensure password String is cleared from memory
            password = null;
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
}
