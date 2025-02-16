package org.example.hvvs.middleware;

import jakarta.ejb.EJB;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.modules.common.service.SessionService;
import org.example.hvvs.utils.CommonParam;
import org.example.hvvs.utils.SessionCacheManager;
import org.example.hvvs.model.Users;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Middleware implements Filter {

    @EJB
    private SessionService sessionService;

    @EJB
    private SessionCacheManager sessionCacheManager;

    // Public paths that don't require authentication
    private static final String[] PUBLIC_PATHS = {
            "/auth/sign-in.xhtml",
            "/index.jsp",
            "/forget-password.xhtml",
            "/404.xhtml",
            "/jakarta.faces.resource/*"
    };

    private static final String MFA_PATH = "/auth/mfa.xhtml";

    // Role-specific path mappings
    private static final List<PathRole> PATH_ROLES = Arrays.asList(
            new PathRole("/resident/", Users.Role.RESIDENT),
            new PathRole("/security/", Users.Role.SECURITY_STAFF),
            new PathRole("/admin/", Users.Role.MANAGING_STAFF)
    );

    // Session expiration thresholds
    private static final long SESSION_DURATION = 8 * 60 * 60 * 1000; // 8 hours
    private static final long RENEWAL_THRESHOLD = 30 * 60 * 1000; // 30 minutes
    private static final long LAST_ACCESS_UPDATE_INTERVAL = 60_000; // 1 minute

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getServletPath();

        // Special handling for MFA page
        if (path.equals(MFA_PATH)) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute(CommonParam.PRE_AUTH_USER) == null) {
                redirectToLogin(req, resp);
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // Bypass filter for public resources
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Users user = null;
        UUID sessionId = null;
        Long expiresAt = null;

        // Extract session information
        if (session != null) {
            user = (Users) session.getAttribute(CommonParam.SESSION_SELF);
            sessionId = (UUID) session.getAttribute(CommonParam.SESSION_ID);
            expiresAt = (Long) session.getAttribute(CommonParam.SESSION_EXPIRES_AT);
        }

        // Session validation logic
        if (user != null && sessionId != null && expiresAt != null) {
            long currentTime = System.currentTimeMillis();

            // Check cache first
            boolean validInCache = sessionCacheManager.isSessionValid(sessionId);
            boolean needsRenewal = (expiresAt - currentTime) < RENEWAL_THRESHOLD;

            if (validInCache && !needsRenewal) {
                handleValidSession(req, resp, chain, user, session, sessionId, path);
                return;
            }

            // Cache miss or needs renewal - validate with database
            Optional<UserSessions> dbSession = sessionService.validateSession(sessionId);
            if (dbSession.isPresent()) {
                UserSessions validSession = dbSession.get();

                // Update session expiration if needed
                if (needsRenewal) {
                    renewSession(session, sessionId, currentTime);
                }

                // Update cache and handle access
                sessionCacheManager.cacheSessionExpiration(sessionId, validSession.getExpiresAt().getTime());
                updateLastAccess(session, sessionId);
                handleValidSession(req, resp, chain, user, session, sessionId, path);
                return;
            } else {
                // Invalid session - clear all traces
                session.invalidate();
                sessionCacheManager.invalidateSession(sessionId);
            }
        }

        // Redirect to login if no valid session
        redirectToLogin(req, resp);
    }

    private void handleValidSession(HttpServletRequest req, HttpServletResponse resp, FilterChain chain,
                                    Users user, HttpSession session, UUID sessionId, String path)
            throws IOException, ServletException {

        // Redirect logged-in users away from public pages
        if (isPublicPath(path)) {
            redirectToDashboard(req, resp, user.getRole());
            return;
        }

        // Check authorization
        if (!isAuthorized(path, user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Update last access (throttled)
        updateLastAccess(session, sessionId);
        chain.doFilter(req, resp);
    }

    private void renewSession(HttpSession session, UUID sessionId, long currentTime) {
        Timestamp newExpiresAt = new Timestamp(currentTime + SESSION_DURATION);
        sessionService.updateSessionExpiration(sessionId, newExpiresAt);
        sessionCacheManager.cacheSessionExpiration(sessionId, newExpiresAt.getTime());
        session.setAttribute(CommonParam.SESSION_EXPIRES_AT, newExpiresAt.getTime());
    }

    private void updateLastAccess(HttpSession session, UUID sessionId) {
        Long lastUpdate = (Long) session.getAttribute(CommonParam.SESSION_LAST_UPDATE);
        long currentTime = System.currentTimeMillis();

        if (lastUpdate == null || (currentTime - lastUpdate) > LAST_ACCESS_UPDATE_INTERVAL) {
            session.setAttribute(CommonParam.SESSION_LAST_UPDATE, currentTime);
            sessionService.updateLastAccessAsync(sessionId);
        }
    }

    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (publicPath.endsWith("/*")) {
                String basePath = publicPath.substring(0, publicPath.indexOf("/*"));
                if (path.startsWith(basePath)) return true;
            } else if (path.equals(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAuthorized(String path, Users.Role role) {
        return PATH_ROLES.stream()
                .filter(pr -> path.startsWith(pr.path))
                .anyMatch(pr -> pr.role == role);
    }

    private void redirectToDashboard(HttpServletRequest req, HttpServletResponse resp, Users.Role role)
            throws IOException {
        String contextPath = req.getContextPath();
        switch (role) {
            case RESIDENT -> resp.sendRedirect(contextPath + "/resident/requests.xhtml");
            case SECURITY_STAFF -> resp.sendRedirect(contextPath + "/security/onboard-visitors.xhtml");
            case MANAGING_STAFF, SUPER_ADMIN -> resp.sendRedirect(contextPath + "/admin/dashboard.xhtml");
            default -> resp.sendRedirect(contextPath + "/auth/sign-in.xhtml");
        }
    }

    private void redirectToLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/auth/sign-in.xhtml");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}

    // Helper class for path-role mapping
    private static class PathRole {
        final String path;
        final Users.Role role;

        PathRole(String path, Users.Role role) {
            this.path = path;
            this.role = role;
        }
    }
}