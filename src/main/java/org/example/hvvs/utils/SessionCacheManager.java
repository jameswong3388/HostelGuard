package org.example.hvvs.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
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
}