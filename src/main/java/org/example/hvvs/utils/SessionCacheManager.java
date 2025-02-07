package org.example.hvvs.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
@Startup
public class SessionCacheManager {
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

    private final Cache<String, Integer> attemptCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    private final Cache<String, Boolean> blockedCache = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();

    public void cacheSessionExpiration(UUID sessionId, long expiresAt) {
        sessionExpirationCache.put(sessionId, expiresAt);
    }

    public boolean isSessionValid(UUID sessionId) {
        Long expiresAt = sessionExpirationCache.getIfPresent(sessionId);
        return expiresAt != null && expiresAt > System.currentTimeMillis();
    }

    public void invalidateSession(UUID sessionId) {
        sessionExpirationCache.invalidate(sessionId);
    }

    public boolean isBlocked(String key) {
        return blockedCache.getIfPresent(key) != null;
    }

    public int incrementAttempt(String key) {
        Integer attempts = attemptCache.get(key, k -> 0);
        attempts++;
        attemptCache.put(key, attempts);
        return attempts;
    }

    public void blockAccess(String key, int seconds) {
        blockedCache.put(key, true);
    }

    public void resetAttempts(String key) {
        attemptCache.invalidate(key);
        blockedCache.invalidate(key);
    }
}