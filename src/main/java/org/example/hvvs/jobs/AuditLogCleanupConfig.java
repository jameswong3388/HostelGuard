package org.example.hvvs.jobs;

import jakarta.ejb.Singleton;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configuration for the audit log cleanup job.
 * This class provides configurable settings for the audit log cleanup process.
 */
@Singleton
@ApplicationScoped
public class AuditLogCleanupConfig {
    
    // Default retention period in minutes (129600 minutes = 90 days = approximately 3 months)
    private int retentionMinutes = 129600;
    
    // Flag to enable or disable the cleanup job
    private boolean enabled = true;
    
    // Default interval in minutes (1440 minutes = 24 hours = daily)
    private int intervalMinutes = 1440;
    
    /**
     * Gets the retention period in minutes.
     * Audit logs older than this many minutes will be deleted.
     * 
     * @return The retention period in minutes
     */
    public int getRetentionMinutes() {
        return retentionMinutes;
    }
    
    /**
     * Sets the retention period in minutes.
     * 
     * @param retentionMinutes The number of minutes to retain audit logs
     */
    public void setRetentionMinutes(int retentionMinutes) {
        if (retentionMinutes < 1) {
            throw new IllegalArgumentException("Retention minutes must be at least 1");
        }
        this.retentionMinutes = retentionMinutes;
    }
    
    /**
     * Gets the retention period in days.
     * Audit logs older than this many days will be deleted.
     * 
     * @return The retention period in days
     * @deprecated Use getRetentionMinutes() instead
     */
    @Deprecated
    public int getRetentionDays() {
        return retentionMinutes / (24 * 60);
    }
    
    /**
     * Sets the retention period in days.
     * 
     * @param retentionDays The number of days to retain audit logs
     * @deprecated Use setRetentionMinutes() instead
     */
    @Deprecated
    public void setRetentionDays(int retentionDays) {
        if (retentionDays < 1) {
            throw new IllegalArgumentException("Retention days must be at least 1");
        }
        this.retentionMinutes = retentionDays * 24 * 60;
    }
    
    /**
     * Gets the cleanup interval in minutes.
     * The cleanup job will run every this many minutes.
     * 
     * @return The cleanup interval in minutes
     */
    public int getIntervalMinutes() {
        return intervalMinutes;
    }
    
    /**
     * Sets the cleanup interval in minutes.
     * 
     * @param intervalMinutes The number of minutes between cleanup job runs
     */
    public void setIntervalMinutes(int intervalMinutes) {
        if (intervalMinutes < 1) {
            throw new IllegalArgumentException("Interval minutes must be at least 1");
        }
        this.intervalMinutes = intervalMinutes;
    }
    
    /**
     * Gets the cleanup interval in hours (for backward compatibility).
     * 
     * @return The cleanup interval in hours
     * @deprecated Use getIntervalMinutes() instead
     */
    @Deprecated
    public int getIntervalHours() {
        return intervalMinutes / 60;
    }
    
    /**
     * Sets the cleanup interval in hours (for backward compatibility).
     * 
     * @param intervalHours The number of hours between cleanup job runs
     * @deprecated Use setIntervalMinutes() instead
     */
    @Deprecated
    public void setIntervalHours(int intervalHours) {
        this.intervalMinutes = intervalHours * 60;
    }
    
    /**
     * Checks if the cleanup job is enabled.
     * 
     * @return true if the cleanup job is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether the cleanup job is enabled.
     * 
     * @param enabled true to enable the cleanup job, false to disable it
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
} 