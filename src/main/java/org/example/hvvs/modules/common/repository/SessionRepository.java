package org.example.hvvs.modules.common.repository;

import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    UserSessions create(UserSessions session);

    UserSessions update(UserSessions session);

    void updateAsync(UserSessions session);

    void revokeSession(UUID sessionId);

    void revokeAllSessions(Integer userId);

    List<UserSessions> findByUser(Users user);

    UserSessions findBySessionId(UUID sessionId);

    void updateSessionExpiration(UUID sessionId, Timestamp newExpiresAt);

    List<UserSessions> findActiveSessionsByUser(Users user);

    void updateSessionAccess(UUID sessionId, Timestamp lastAccess, Timestamp newExpiresAt);
}
