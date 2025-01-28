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

import org.example.hvvs.model.Users;
import org.example.hvvs.modules.auth.service.AuthServices;
import org.example.hvvs.utils.CommonParam;

/**
 * Login Filter<br/>
 * 1. If already logged in (admin or user), and request is a public page, redirect to dashboard.<br/>
 * 2. If auto-login cookie exists, verify validity. If valid then login and allow through, otherwise delete cookie<br/>
 * 3. If accessing a page that doesn't require login, allow through<br/>
 * 4. Otherwise, redirect to login page<br/>
 * 5. After ensuring login, check if user’s role matches the path they are accessing.
 *
 * @author ...
 */
public class Middleware implements Filter {
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
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String servletPath = req.getServletPath();

        // Allow JSF resources (CSS, JS, images) to pass through
        if (servletPath.startsWith("/jakarta.faces.resource/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Users user = null;

        if (session != null) {
            user = (Users) session.getAttribute(CommonParam.SESSION_SELF);
        }

        // ---------------------------------------------------------------------
        // 1. If user is already logged in:
        //    - If requesting a public page, redirect to user's dashboard
        //    - Otherwise, let the user access the requested page (but check role below)
        // ---------------------------------------------------------------------
        if (user != null) {
            if (isUnLoginPage(servletPath)) {
                // Redirect to appropriate dashboard based on user's role
                redirectToDashboard(req, resp, user.getRole());
                return;
            }

            // ---------------------------------------------------------
            // 5. Role-based check: ensure the user’s role matches path
            // ---------------------------------------------------------
            if (!isAuthorizedForPath(servletPath, user.getRole())) {
                // If user doesn’t have permission, respond with 403 or redirect to an error page.
                // Here we send 403 Forbidden:
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // If role check passes, proceed
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

                // ---------------------------------------------------------
                // 5. Role-based check after auto-login
                // ---------------------------------------------------------
                if (!isAuthorizedForPath(servletPath, user.getRole())) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
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
            throws IOException {
        String contextPath = req.getServletContext().getContextPath();
        switch (role) {
            case CommonParam.SESSION_ROLE_RESIDENT:
                resp.sendRedirect(contextPath + "/resident/requests.xhtml");
                break;
            case CommonParam.SESSION_ROLE_SECURITY_STAFF:
                resp.sendRedirect(contextPath + "/security/onboard-visitors.xhtml");
                break;
            case CommonParam.SESSION_ROLE_MANAGING_STAFF:
                resp.sendRedirect(contextPath + "/admin/dashboard.xhtml");
                break;
            default:
                // If the role is unrecognized, fallback to login
                resp.sendRedirect(contextPath + "/auth.xhtml");
                break;
        }
    }

    /**
     * Utility: check if the user’s role grants access to the requested path.
     */
    private boolean isAuthorizedForPath(String servletPath, String role) {
        // If path starts with /resident => only resident role can access
        if (servletPath.startsWith("/resident")) {
            return CommonParam.SESSION_ROLE_RESIDENT.equals(role);
        }
        // If path starts with /security => only security staff can access
        if (servletPath.startsWith("/security")) {
            return CommonParam.SESSION_ROLE_SECURITY_STAFF.equals(role);
        }
        // If path starts with /admin => only managing staff can access
        if (servletPath.startsWith("/admin")) {
            return CommonParam.SESSION_ROLE_MANAGING_STAFF.equals(role);
        }
        // If none of the above prefixes, you can decide whether it's open
        // to all authenticated user or no one. For simplicity, let's allow
        // any authenticated user if it doesn't match any prefix:
        return true;
    }
}
