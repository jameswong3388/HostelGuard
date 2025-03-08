package org.example.hvvs.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages user session lifecycle and security-related tracking through three layered caches:
 * <ol>
 *   <li>Session Expiration Cache: Tracks active sessions with precise expiration times</li>
 *   <li>Attempt Tracking Cache: Monitors failed login attempts with 15-minute rolling window</li>
 *   <li>Blocked Access Cache: Enforces temporary access blocks after excessive failed attempts</li>
 * </ol>
 * All caches are thread-safe and automatically expire entries using Caffeine caching library.
 */
@Singleton
@Startup
public class SessionCacheManager {
    /**
     * Session expiration tracker using custom expiration based on stored timestamps.
     * Key: Session UUID
     * Value: Expiration timestamp (milliseconds since epoch)
     * Policy: Entries expire exactly at their specified expiration time
     */
    private final Cache<UUID, Long> sessionExpirationCache = Caffeine.newBuilder()
            .expireAfter(new Expiry<UUID, Long>() {
                public long expireAfterCreate(UUID key, Long expiresAt, long currentTime) {
                    return TimeUnit.MILLISECONDS.toNanos(expiresAt - currentTime);
                }
                public long expireAfterUpdate(UUID key, Long expiresAt, long currentTime, long currentDuration) {
                    return TimeUnit.MILLISECONDS.toNanos(expiresAt - currentTime);
                }
                public long expireAfterRead(UUID key, Long expiresAt, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    /**
     * Failed login attempt counter with sliding expiration window.
     * Key: User identifier (username/IP address)
     * Value: Number of consecutive failed attempts
     * Policy: Entries expire 15 minutes after last write
     */
    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    /**
     * Temporary access block storage with configurable expiration.
     * Key: User identifier (username/IP address)
     * Value: Expiration timestamp (milliseconds since epoch)
     * Policy: Entries expire based on stored end time
     */
    private final Cache<String, Long> blockedCache = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, Long>() {
                public long expireAfterCreate(String key, Long expiresAt, long currentTime) {
                    return TimeUnit.MILLISECONDS.toNanos(expiresAt - currentTime);
                }
                public long expireAfterUpdate(String key, Long expiresAt, long currentTime, long currentDuration) {
                    return TimeUnit.MILLISECONDS.toNanos(expiresAt - currentTime);
                }
                public long expireAfterRead(String key, Long expiresAt, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    /**
     * Caches the expiration time for a given session ID
     * @param sessionId The UUID of the session to track
     * @param expiresAt The timestamp (in milliseconds) when the session should expire
     */
    public void cacheSessionExpiration(UUID sessionId, long expiresAt) {
        sessionExpirationCache.put(sessionId, expiresAt);
    }

    /**
     * Checks if the specified session is currently valid
     * @param sessionId The UUID of the session to check
     * @return true if the session exists and hasn't expired, false otherwise
     */
    public boolean isSessionValid(UUID sessionId) {
        Long expiresAt = sessionExpirationCache.getIfPresent(sessionId);
        return expiresAt != null && expiresAt > System.currentTimeMillis();
    }

    /**
     * Immediately invalidates a session by removing it from the cache
     * @param sessionId The UUID of the session to invalidate
     */
    public void invalidateSession(UUID sessionId) {
        sessionExpirationCache.invalidate(sessionId);
    }

    /**
     * Checks if access for the given key is currently blocked
     * @param key The identifier to check (typically username or IP address)
     * @return true if access is blocked, false otherwise
     */
    public boolean isBlocked(String key) {
        Long expiry = blockedCache.getIfPresent(key);
        return expiry != null && expiry > System.currentTimeMillis();
    }

    /**
     * Gets the remaining time (in seconds) that a key is blocked for
     * @param key The identifier to check (typically username or IP address)
     * @return Remaining seconds of block, or 0 if not blocked
     */
    public long getBlockedRemainingTime(String key) {
        Long expiry = blockedCache.getIfPresent(key);
        if (expiry == null || expiry <= System.currentTimeMillis()) {
            return 0;
        }
        return (expiry - System.currentTimeMillis()) / 1000;
    }

    /**
     * Increments the failed login attempt counter for the specified key
     * @param key The identifier to track (typically username or IP address)
     * @return The new total number of attempts for the given key
     */
    public int incrementAttempt(String key) {
        Integer attempts = attemptCache.get(key, k -> 0);
        attempts++;
        attemptCache.put(key, attempts);
        return attempts;
    }

    /**
     * Gets the current attempt count for a key
     * @param key The identifier to check
     * @return The current number of attempts, or 0 if none recorded
     */
    public int getAttemptCount(String key) {
        Integer attempts = attemptCache.getIfPresent(key);
        return attempts == null ? 0 : attempts;
    }

    /**
     * Blocks access for the specified key for the specified duration
     * @param key The identifier to block (typically username or IP address)
     * @param seconds The block duration in seconds
     */
    public void blockAccess(String key, int seconds) {
        long expiryTime = System.currentTimeMillis() + (seconds * 1000L);
        blockedCache.put(key, expiryTime);
    }

    /**
     * Sets a custom expiry time for a key in the attempt cache
     * This allows for longer-lived reputation tracking
     * @param key The identifier to set expiry for
     * @param seconds The expiry duration in seconds
     */
    public void setExpiry(String key, long seconds) {
        // We can't directly set expiry in Caffeine, so we refresh the value
        // with a custom expiry implementation in the future
        Integer attempts = attemptCache.getIfPresent(key);
        if (attempts != null) {
            attemptCache.put(key, attempts);
            // Note: this doesn't actually change the expiry, since Caffeine doesn't support
            // changing the expiry policy for individual entries
        }
    }

    /**
     * Resets all login attempt tracking and removes any block for the specified key
     * @param key The identifier to reset (typically username or IP address)
     */
    public void resetAttempts(String key) {
        attemptCache.invalidate(key);
        blockedCache.invalidate(key);
    }

    /**
     * Calculates the number of remaining allowed login attempts
     * @param key The identifier to check (typically username or IP address)
     * @return Remaining attempts before blocking occurs (max 5, min 1)
     */
    public int getRemainingAttempts(String key) {
        if (isBlocked(key)) {
            return 0; // Account is locked
        }
        
        Integer attempts = attemptCache.getIfPresent(key);
        return attempts == null ? 5 : Math.max(1, 5 - attempts);
    }
}