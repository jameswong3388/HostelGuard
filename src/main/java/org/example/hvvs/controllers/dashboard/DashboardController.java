package org.example.hvvs.controllers.dashboard;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.model.User;
import org.example.hvvs.util.CommonParam;

import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class DashboardController implements Serializable {

    public String logout() throws IOException {
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

    public String getCurrentUserName() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(CommonParam.SESSION_SELF);
            if (user != null) {
                return user.getFullName();
            }
        }
        return "User";
    }

    public String getCurrentUserInitials() {
        String fullName = getCurrentUserName();
        if (fullName != null && !fullName.isEmpty()) {
            String[] names = fullName.split(" ");
            if (names.length > 1) {
                return (names[0].charAt(0) + "" + names[names.length-1].charAt(0)).toUpperCase();
            } else {
                return fullName.substring(0, Math.min(2, fullName.length())).toUpperCase();
            }
        }
        return "U";
    }
}