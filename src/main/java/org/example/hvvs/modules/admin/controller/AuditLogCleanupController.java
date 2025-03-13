package org.example.hvvs.modules.admin.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.example.hvvs.jobs.AuditLogCleanupConfig;
import org.example.hvvs.jobs.AuditLogCleanupJob;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ejb.EJB;

/**
 * Controller for managing audit log cleanup settings.
 */
@Named
@ViewScoped
public class AuditLogCleanupController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AuditLogCleanupController.class.getName());
    
    @Inject
    private AuditLogCleanupConfig config;
    
    @EJB
    private AuditLogCleanupJob auditLogCleanupJob;
    
    private int retentionMinutes;
    private boolean enabled;
    private int intervalMinutes;
    
    @PostConstruct
    public void init() {
        // Load current configuration
        this.retentionMinutes = config.getRetentionMinutes();
        this.enabled = config.isEnabled();
        this.intervalMinutes = config.getIntervalMinutes();
    }
    
    /**
     * Saves the audit log cleanup configuration.
     */
    public void saveConfig() {
        try {
            // Store previous values to check for changes
            boolean wasEnabled = config.isEnabled();
            int previousInterval = config.getIntervalMinutes();
            
            // Update configuration
            config.setRetentionMinutes(retentionMinutes);
            config.setEnabled(enabled);
            config.setIntervalMinutes(intervalMinutes);
            
            LOGGER.log(Level.INFO, "Audit log cleanup configuration updated: retention={0} minutes, enabled={1}, interval={2} minutes", 
                    new Object[]{retentionMinutes, enabled, intervalMinutes});
            
            // Reset the timer if the status changed or interval changed
            if (wasEnabled != enabled || previousInterval != intervalMinutes) {
                auditLogCleanupJob.resetTimer();
                LOGGER.log(Level.INFO, "Audit log cleanup timer reset triggered after configuration change");
            }
            
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Audit log cleanup settings saved successfully"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving audit log cleanup configuration", e);
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save settings: " + e.getMessage()));
        }
    }
    
    /**
     * Manually runs the cleanup job once.
     * This is useful for testing the configuration.
     */
    public void runJobNow() {
        try {
            LOGGER.log(Level.INFO, "Manual execution of audit log cleanup job requested");
            auditLogCleanupJob.cleanupOldAuditLogs();
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Audit log cleanup job executed manually"));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error executing audit log cleanup job manually", e);
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to execute job: " + e.getMessage()));
        }
    }
    
    // Getters and setters
    
    public int getRetentionMinutes() {
        return retentionMinutes;
    }
    
    public void setRetentionMinutes(int retentionMinutes) {
        this.retentionMinutes = retentionMinutes;
    }
    
    /**
     * @deprecated Use getRetentionMinutes() instead
     */
    @Deprecated
    public int getRetentionDays() {
        return retentionMinutes / (24 * 60);
    }
    
    /**
     * @deprecated Use setRetentionMinutes() instead
     */
    @Deprecated
    public void setRetentionDays(int retentionDays) {
        this.retentionMinutes = retentionDays * 24 * 60;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getIntervalMinutes() {
        return intervalMinutes;
    }
    
    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }
    
    /**
     * @deprecated Use getIntervalMinutes() instead
     */
    @Deprecated
    public int getIntervalHours() {
        return intervalMinutes / 60;
    }
    
    /**
     * @deprecated Use setIntervalMinutes() instead
     */
    @Deprecated
    public void setIntervalHours(int intervalHours) {
        this.intervalMinutes = intervalHours * 60;
    }
} 