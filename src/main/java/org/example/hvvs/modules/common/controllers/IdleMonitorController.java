package org.example.hvvs.modules.common.controllers;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.UsersFacade;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.SessionCacheManager;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Named("IdleMonitorController")
@SessionScoped
public class IdleMonitorController implements Serializable {
    @EJB
    private SessionService sessionService;

    @EJB
    private SessionCacheManager sessionCacheManager;

    @EJB
    private UsersFacade usersFacade;

    public String handleIdle() {
        try {
            HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                    .getExternalContext().getSession(false);

            if (session != null) {
                UUID sessionId = (UUID) session.getAttribute(CommonParam.SESSION_ID);
                Users user = (Users) session.getAttribute(CommonParam.SESSION_SELF);

                if (sessionId != null && user != null) {
                    // Immediate session revocation
                    sessionService.revokeSession(sessionId);
                    sessionCacheManager.invalidateSession(sessionId);

                    // Force check for any remaining sessions
                    List<UserSessions> activeSessions = sessionService.getActiveSessions(user);
                    if (activeSessions.isEmpty()) {
                        user.setIsActive(false);
                        usersFacade.edit(user);  // Make sure to persist the status change
                    }
                }
                session.invalidate();
            }

            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Session Expired",
                            "Your session has expired due to inactivity."));

            return "/auth/sign-in.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Error handling idle timeout " + e.getMessage()));
            return "/auth/sign-in.xhtml?faces-redirect=true";
        }
    }

    public void onIdle() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "No activity.", "User is idle"));
    }

    public void onActive() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                "Activity detected", "User is active"));
    }
}