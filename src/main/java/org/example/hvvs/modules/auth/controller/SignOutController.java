package org.example.hvvs.modules.auth.controller;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.util.CommonParam;

import java.io.IOException;
import java.io.Serializable;

@Named("signOutController")
@SessionScoped
public class SignOutController implements Serializable {

    public String signOut() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext externalContext = context.getExternalContext();

        // Invalidate session
        HttpSession session = (HttpSession) externalContext.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Set auto-login cookie to expire
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        Cookie cookie = new Cookie(CommonParam.COOKIE_AUTO_LOGIN, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Return navigation outcome
        return "/auth.xhtml";
    }
}
