package org.example.hvvs.middleware;

import java.io.IOException;

import jakarta.ejb.EJB;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.model.User;
import org.example.hvvs.services.AuthServices;
import org.example.hvvs.util.CookieSessionParam;


/**
 * Login Filter<br/>
 * For all pages:<br/>
 * 1. If already logged in (admin or user), allow through<br/>
 * 2. If auto-login cookie exists, verify validity. If valid then login and allow through, otherwise delete cookie<br/>
 * 3. If accessing servlet page that doesn't require login, allow through
 * 4. Otherwise, redirect to login page
 *
 * @author hkq
 */

public class LoginFilter implements Filter {
    @EJB
    private AuthServices authServices;

    // Servlet paths that don't require login
    public static final String[] unLoginServletPathes = {
            "/auth", "/login", "/home"
    };

    @Override
    public void init(FilterConfig config) throws ServletException {
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String servletPath = req.getServletPath();

        // Check if user is already logged in
        if (session != null && session.getAttribute(CookieSessionParam.SESSION_SELF) != null) {
            // User is logged in, allow request to proceed
            chain.doFilter(request, response);
            return;
        }

        // Auto-login check
        Cookie[] cookies = req.getCookies();
        Cookie cookie = null;
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (CookieSessionParam.COOKIE_AUTO_LOGIN.equals(c.getName())) {
                    cookie = c;
                    break;
                }
            }
        }

        // Verify cookie validity
        if (cookie != null) {
            int id = authServices.allowAutoLogin(cookie.getValue());
            if (id != -1) { // Valid cookie - create session and allow through
                User user = authServices.findUser(id);
                session = req.getSession(true);
                session.setAttribute(CookieSessionParam.SESSION_ROLE, user.getRole());
                session.setAttribute(CookieSessionParam.SESSION_SELF, user);

                // Redirect to appropriate dashboard if accessing auth pages
                if (servletPath.equals("/auth") || servletPath.equals("/login")) {
                    String contextPath = req.getServletContext().getContextPath();
                    switch (user.getRole()) {
                        case CookieSessionParam.SESSION_ROLE_RESIDENT:
                            resp.sendRedirect(contextPath + "/resident/dashboard.jsp");
                            return;
                        case CookieSessionParam.SESSION_ROLE_SECURITY_STAFF:
                            resp.sendRedirect(contextPath + "/security/dashboard.jsp");
                            return;
                        case CookieSessionParam.SESSION_ROLE_ADMIN:
                            resp.sendRedirect(contextPath + "/admin/dashboard.jsp");
                            return;
                        case CookieSessionParam.SESSION_ROLE_SUPER_ADMIN:
                            resp.sendRedirect(contextPath + "/admin/super/dashboard.jsp");
                            return;
                    }
                }

                chain.doFilter(request, response);
                return;
            } else { // Invalid cookie - delete it
                cookie.setMaxAge(0);
                resp.addCookie(cookie);
            }
        }

        // If path doesn't require login, allow through
        for (String path : unLoginServletPathes) {
            if (path.equals(servletPath)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // Redirect to login page
        resp.sendRedirect(req.getServletContext().getContextPath() + "/auth");
    }

    @Override
    public void destroy() {

    }
}
