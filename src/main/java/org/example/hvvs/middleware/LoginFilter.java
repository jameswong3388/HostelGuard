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
import org.example.hvvs.util.CommonParam;

/**
 * Login Filter<br/>
 * For all pages:<br/>
 * 1. If already logged in (admin or user), and request is a public page, redirect to dashboard.<br/>
 * 2. If auto-login cookie exists, verify validity. If valid then login and allow through, otherwise delete cookie<br/>
 * 3. If accessing a page that doesn't require login, allow through<br/>
 * 4. Otherwise, redirect to login page
 *
 * @author ...
 */
public class LoginFilter implements Filter {
    @EJB
    private AuthServices authServices;

    // Servlet paths that don't require login
    public static final String[] unLoginServletPathes = {
            "/auth.xhtml",
            "/index.xhtml",
            "/forget-password.xhtml",
            "/404.xhtml",
            "/jakarta.faces.resource/*"  // Important for JSF resources
    };

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String servletPath       = req.getServletPath();

        // Allow JSF resources (CSS, JS, images) to pass through
        if (servletPath.startsWith("/jakarta.faces.resource/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        User        user    = null;

        if (session != null) {
            user = (User) session.getAttribute(CommonParam.SESSION_SELF);
        }

        // ---------------------------------------------------------------------
        // 1. If user is already logged in:
        //    - If requesting a public page, redirect to user's dashboard
        //    - Otherwise, let the user access the requested page
        // ---------------------------------------------------------------------
        if (user != null) {
            if (isUnLoginPage(servletPath)) {
                // Redirect to appropriate dashboard based on user's role
                redirectToDashboard(req, resp, user.getRole());
                return;
            }
            // Otherwise, user is logged in and requesting a secured page
            chain.doFilter(request, response);
            return;
        }

        // ---------------------------------------------------------------------
        // 2. Handle auto-login cookie
        // ---------------------------------------------------------------------
        Cookie autoLoginCookie = getCookieByName(req, CommonParam.COOKIE_AUTO_LOGIN);
        if (autoLoginCookie != null) {
            int userId = authServices.allowAutoLogin(autoLoginCookie.getValue());
            if (userId != -1) {
                // Valid auto-login cookie, create session
                user = authServices.findUser(userId);
                session = req.getSession(true);
                session.setAttribute(CommonParam.SESSION_ROLE, user.getRole());
                session.setAttribute(CommonParam.SESSION_SELF, user);

                // If the request was for a public page,
                // redirect to the user's dashboard after auto-login
                if (isUnLoginPage(servletPath)) {
                    redirectToDashboard(req, resp, user.getRole());
                    return;
                }
                // Otherwise, allow access
                chain.doFilter(request, response);
                return;
            } else {
                // Invalid cookie - delete it
                autoLoginCookie.setMaxAge(0);
                resp.addCookie(autoLoginCookie);
            }
        }

        // ---------------------------------------------------------------------
        // 3. If the requested path doesn't require login, let it pass
        // ---------------------------------------------------------------------
        if (isUnLoginPage(servletPath)) {
            chain.doFilter(request, response);
            return;
        }

        // ---------------------------------------------------------------------
        // 4. Otherwise, user is not logged in, so redirect to login page
        // ---------------------------------------------------------------------
        resp.sendRedirect(req.getServletContext().getContextPath() + "/auth.xhtml");
    }

    @Override
    public void destroy() {
    }

    /**
     * Utility: checks if the requested servlet path is in the list of pages
     * that do not require login.
     */
    private boolean isUnLoginPage(String servletPath) {
        for (String path : unLoginServletPathes) {
            // if it contains a wildcard (like "/jakarta.faces.resource/*")
            if (path.endsWith("/*")) {
                String trimmed = path.substring(0, path.indexOf("/*"));
                if (servletPath.startsWith(trimmed)) {
                    return true;
                }
            } else {
                if (servletPath.equals(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Utility: get a specific cookie by name
     */
    private Cookie getCookieByName(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (cookieName.equals(c.getName())) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Utility: redirect to a user’s dashboard based on their role
     */
    private void redirectToDashboard(HttpServletRequest req, HttpServletResponse resp, String role)
            throws IOException
    {
        String contextPath = req.getServletContext().getContextPath();
        switch (role) {
            case CommonParam.SESSION_ROLE_RESIDENT:
                resp.sendRedirect(contextPath + "/resident/dashboard.xhtml");
                break;
            case CommonParam.SESSION_ROLE_SECURITY_STAFF:
                resp.sendRedirect(contextPath + "/security/dashboard.xhtml");
                break;
            case CommonParam.SESSION_ROLE_MANAGING_STAFF:
                resp.sendRedirect(contextPath + "/admin/dashboard.xhtml");
                break;
            default:
                // If the role is unrecognized, fallback to a default
                resp.sendRedirect(contextPath + "/auth.xhtml");
                break;
        }
    }
}
