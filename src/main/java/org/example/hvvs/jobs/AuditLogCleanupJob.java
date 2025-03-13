package org.example.hvvs.jobs;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.AuditLogsFacade;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduled job to clean up old audit logs.
 * This job runs at configurable intervals and removes audit logs older than a specified retention period.
 */
@Singleton
@Startup
public class AuditLogCleanupJob {

    private static final Logger LOGGER = Logger.getLogger(AuditLogCleanupJob.class.getName());
    
    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;
    
    @EJB
    private AuditLogsFacade auditLogsFacade;
    
    @Inject
    private AuditLogCleanupConfig config;
    
    @Resource
    private TimerService timerService;
    
    @PostConstruct
    public void init() {
        // Cancel any existing timers
        for (Timer timer : timerService.getTimers()) {
            if ("AuditLogCleanup".equals(timer.getInfo())) {
                timer.cancel();
            }
        }
        
        // Create a new timer based on the configured interval
        createTimer();
        
        LOGGER.log(Level.INFO, "Audit log cleanup job initialized with interval of {0} minutes", 
                config.getIntervalMinutes());
    }
    
    /**
     * Creates a timer for the audit log cleanup job based on the configured interval.
     */
    public void createTimer() {
        // Convert minutes to milliseconds
        long intervalMillis = config.getIntervalMinutes() * 60 * 1000L;
        
        // Create a non-persistent timer that runs at the specified interval
        TimerConfig timerConfig = new TimerConfig();
        timerConfig.setInfo("AuditLogCleanup");
        timerConfig.setPersistent(false);
        
        timerService.createIntervalTimer(0, intervalMillis, timerConfig);
    }
    
    /**
     * Resets the timer with updated configuration.
     * This method should be called when the job configuration is updated.
     */
    public void resetTimer() {
        // Cancel any existing timers
        for (Timer timer : timerService.getTimers()) {
            if ("AuditLogCleanup".equals(timer.getInfo())) {
                timer.cancel();
                LOGGER.log(Level.INFO, "Cancelled existing audit log cleanup timer");
            }
        }
        
        // Create a new timer with updated interval
        createTimer();
        
        LOGGER.log(Level.INFO, "Audit log cleanup timer reset with interval of {0} minutes", 
                config.getIntervalMinutes());
    }
    
    /**
     * Timeout method that is called when the timer expires.
     * This method performs the actual cleanup of old audit logs.
     */
    @Timeout
    public void cleanupOldAuditLogs() {
        try {
            // Skip if the job is disabled
            if (!config.isEnabled()) {
                LOGGER.log(Level.INFO, "Audit log cleanup job is disabled. Skipping execution.");
                return;
            }
            
            LOGGER.log(Level.INFO, "Starting audit log cleanup job");
            
            // Calculate the cutoff date (current date minus retention period)
            LocalDateTime cutoffDate = LocalDateTime.now().minusMinutes(config.getRetentionMinutes());
            Timestamp cutoffTimestamp = Timestamp.valueOf(cutoffDate);
            
            // Execute delete query for logs older than the cutoff date
            int deletedCount = em.createQuery(
                    "DELETE FROM AuditLogs a WHERE a.created_at < :cutoffDate")
                    .setParameter("cutoffDate", cutoffTimestamp)
                    .executeUpdate();
            
            LOGGER.log(Level.INFO, "Audit log cleanup completed. Deleted {0} old audit logs older than {1} minutes.", 
                    new Object[]{deletedCount, config.getRetentionMinutes()});
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during audit log cleanup", e);
        }
    }
} 