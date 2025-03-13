package org.example.hvvs.modules.common.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.hvvs.model.AuditLogs;
import org.example.hvvs.model.Users;

/**
 * Service for recording audit logs throughout the application.
 */
public interface AuditLogService {
    
    /**
     * Records an audit log for any action in the system.
     * 
     * @param user The user performing the action, can be null for system actions
     * @param action The type of action being performed
     * @param entityType The type of entity being acted upon
     * @param entityId The ID of the entity, can be null
     * @param description A description of the action
     * @param oldValues JSON string of the old values before the action, can be null
     * @param newValues JSON string of the new values after the action, can be null
     * @param additionalData Any additional data to store, can be null
     * @param request The HTTP request, can be null if not in a web context
     * @return The created AuditLogs entry
     */
    AuditLogs log(
        Users user, 
        AuditLogs.Action action, 
        String entityType, 
        String entityId, 
        String description, 
        String oldValues, 
        String newValues, 
        String additionalData, 
        HttpServletRequest request
    );
    
    /**
     * Convenience method to log a create action
     */
    AuditLogs logCreate(
        Users user, 
        String entityType, 
        String entityId, 
        String description, 
        String newValues, 
        HttpServletRequest request
    );
    
    /**
     * Convenience method to log an update action
     */
    AuditLogs logUpdate(
        Users user, 
        String entityType, 
        String entityId, 
        String description, 
        String oldValues, 
        String newValues, 
        HttpServletRequest request
    );
    
    /**
     * Convenience method to log a delete action
     */
    AuditLogs logDelete(
        Users user, 
        String entityType, 
        String entityId, 
        String description, 
        String oldValues, 
        HttpServletRequest request
    );
    
    /**
     * Convenience method to log a read/access action
     */
    AuditLogs logRead(
        Users user, 
        String entityType, 
        String entityId, 
        String description, 
        HttpServletRequest request
    );
    
    /**
     * Convenience method to log a login action
     */
    AuditLogs logLogin(
        Users user, 
        String additionalInfo,
        HttpServletRequest request
    );
    
    /**
     * Convenience method to log a logout action
     */
    AuditLogs logLogout(
        Users user, 
        HttpServletRequest request
    );
}
