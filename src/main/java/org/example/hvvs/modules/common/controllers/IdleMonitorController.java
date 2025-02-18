package org.example.hvvs.modules.common.controllers;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.ServiceResult;

import java.io.Serializable;

@Named("IdleMonitorController")
@SessionScoped
public class IdleMonitorController implements Serializable {
    @EJB
    private AuthServices authServices;

    public String handleIdle() {
        try {
            ServiceResult<Void> result = authServices.signOut();
            
            if (!result.isSuccess()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", result.getMessage()));
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
                            "Error handling idle timeout: " + e.getMessage()));
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