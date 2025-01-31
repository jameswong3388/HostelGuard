package org.example.hvvs.modules.auth.controller;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;


@Named("signOutController")
@SessionScoped
public class SignOutController implements Serializable {

    @EJB private SessionService sessionService;

    public String signOut() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession) ec.getSession(false);

        if (session != null) {
            UUID sessionId = (UUID) session.getAttribute(CommonParam.SESSION_ID);
            if (sessionId != null) {
                sessionService.revokeSession(sessionId);
            }
            session.invalidate();
        }

        ec.redirect(ec.getRequestContextPath() + "/auth/sign-in.xhtml");
        return null;
    }
}
