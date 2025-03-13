package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.example.hvvs.model.Users;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.modules.common.service.AuditLogService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.ServiceResult;

import java.io.IOException;
import java.io.Serializable;

@Named("signOutController")
@SessionScoped
public class SignOutController implements Serializable {

    @EJB
    private AuthServices authServices;

    @EJB
    private AuditLogService auditLogService;

    public String signOut() {
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
                    .getExternalContext().getRequest();
            Users user = (Users) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(CommonParam.SESSION_SELF);
            ServiceResult<Void> result = authServices.signOut(request);
            
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            if (!result.isSuccess()) {
                ec.getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", result.getMessage()));
            }

            auditLogService.logLogout(
                    user,
                    request
            );

            ec.redirect(ec.getRequestContextPath() + "/auth/sign-in.jsp");
            return null;
        } catch (IOException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Redirect failed: " + e.getMessage()));
            return null;
        }
    }
}
