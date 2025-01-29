package org.example.hvvs.modules.common.service;

import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionService {
    UserSessions createSession(Users user, String ipAddress, String userAgent);

    void revokeSession(UUID sessionId);

    void revokeAllSessions(Integer userId);

    List<UserSessions> getActiveSessions(Users user);

    void updateLastAccess(UUID sessionId);

    void updateLastAccessAsync(UUID sessionId);

    void updateSessionExpiration(UUID sessionId, Timestamp newExpiresAt);

    Optional<UserSessions> validateSession(UUID sessionId);

    String parseLocationFromIp(String ipAddress);
}
