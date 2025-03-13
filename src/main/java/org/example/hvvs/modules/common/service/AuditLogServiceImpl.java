package org.example.hvvs.modules.common.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.servlet.http.HttpServletRequest;
import org.example.hvvs.model.AuditLogs;
import org.example.hvvs.model.AuditLogsFacade;
import org.example.hvvs.model.Users;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Implementation of the AuditLogService for recording user actions throughout the application.
 */
@Stateless
public class AuditLogServiceImpl implements AuditLogService {

    @EJB
    private AuditLogsFacade auditLogsFacade;

    @Override
    public AuditLogs log(
            Users user,
            AuditLogs.Action action,
            String entityType,
            String entityId,
            String description,
            String oldValues,
            String newValues,
            String additionalData,
            HttpServletRequest request) {
        
        // Create a new audit log entry
        AuditLogs auditLog = new AuditLogs();
        
        // Set basic information
        auditLog.setUserId(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setAdditionalData(additionalData);
        
        // Extract IP address and user agent from the request if available
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        // Set the timestamp manually (though there's also @PrePersist in the entity)
        auditLog.setCreatedAt(Timestamp.from(Instant.now()));
        
        // Persist the audit log
        auditLogsFacade.create(auditLog);
        
        return auditLog;
    }

    @Override
    public AuditLogs logCreate(
            Users user,
            String entityType,
            String entityId,
            String description,
            String newValues,
            HttpServletRequest request) {
        
        return log(
            user,
            AuditLogs.Action.CREATE,
            entityType,
            entityId,
            description,
            null, // No old values for creation
            newValues,
            null, // No additional data needed by default
            request
        );
    }

    @Override
    public AuditLogs logUpdate(
            Users user,
            String entityType,
            String entityId,
            String description,
            String oldValues,
            String newValues,
            HttpServletRequest request) {
        
        return log(
            user,
            AuditLogs.Action.UPDATE,
            entityType,
            entityId,
            description,
            oldValues,
            newValues,
            null, // No additional data needed by default
            request
        );
    }

    @Override
    public AuditLogs logDelete(
            Users user,
            String entityType,
            String entityId,
            String description,
            String oldValues,
            HttpServletRequest request) {
        
        return log(
            user,
            AuditLogs.Action.DELETE,
            entityType,
            entityId,
            description,
            oldValues,
            null, // No new values for deletion
            null, // No additional data needed by default
            request
        );
    }

    @Override
    public AuditLogs logRead(
            Users user,
            String entityType,
            String entityId,
            String description,
            HttpServletRequest request) {
        
        return log(
            user,
            AuditLogs.Action.READ,
            entityType,
            entityId,
            description,
            null, // No old values for read
            null, // No new values for read
            null, // No additional data needed by default
            request
        );
    }

    @Override
    public AuditLogs logLogin(
            Users user,
            String additionalInfo,
            HttpServletRequest request) {

        String description = "User logged in successfully";

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            description += ": " + additionalInfo;
        }
        
        return log(
            user,
            AuditLogs.Action.LOGIN,
            "USER_SESSIONS",
            user != null ? user.getId().toString() : null,
            description,
            null, // No old values for login
            null, // No new values for login
            null,
            request
        );
    }

    @Override
    public AuditLogs logLogout(Users user, HttpServletRequest request) {
        return log(
            user,
            AuditLogs.Action.LOGOUT,
            "USER_SESSIONS",
            user != null ? user.getId().toString() : null,
            "User logged out",
            null, // No old values for logout
            null, // No new values for logout
            null, // No additional data needed by default
            request
        );
    }

    /**
     * Extract the client IP address from the request, handling proxy scenarios.
     * 
     * @param request The HTTP request
     * @return The client's IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // If it's a comma-separated list (from X-Forwarded-For), take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}
