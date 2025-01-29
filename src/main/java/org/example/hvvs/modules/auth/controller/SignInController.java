package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
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
                // Failed authentication: show the error message
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error", serviceResult.getMessage()));
                return null;
            }

            // If success, retrieve the authenticated user
            Users user = serviceResult.getData();

            // Create Server Session tracking record
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();

            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String deviceInfo = sessionService.parseDeviceInfo(userAgent);

            // Create new session
            UserSessions userSession = sessionService.createSession(user, ipAddress, userAgent, deviceInfo);

            // Store in session
            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            session.setAttribute(CommonParam.SESSION_ID, userSession.getSession_id());
            session.setAttribute(CommonParam.SESSION_ROLE, user.getRole());
            session.setAttribute(CommonParam.SESSION_SELF, user);

            // Now redirect or navigate based on the user's role
            return switch (user.getRole()) {
                case CommonParam.SESSION_ROLE_RESIDENT -> "resident/requests";
                case CommonParam.SESSION_ROLE_SECURITY_STAFF -> "onboard-visitors";
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
}
