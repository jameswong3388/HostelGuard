package org.example.hvvs.modules.common.repository;

import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository {
    UserSessions create(UserSessions session);

    UserSessions update(UserSessions session);

    void revokeSession(UUID sessionId);

    void revokeAllSessions(Integer userId);

    List<UserSessions> findByUser(Users user);

    UserSessions findBySessionId(UUID sessionId);

    void updateLastAccess(UUID sessionId);

    List<UserSessions> findActiveSessionsByUser(Users user);
}
