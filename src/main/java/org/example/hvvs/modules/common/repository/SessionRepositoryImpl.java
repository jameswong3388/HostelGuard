package org.example.hvvs.modules.common.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Stateless
public class SessionRepositoryImpl implements SessionRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public UserSessions create(UserSessions session) {
        em.persist(session);
        return session;
    }

    @Override
    public UserSessions update(UserSessions session) {
        return em.merge(session);
    }

    @Override
    public void updateAsync(UserSessions session) {
        em.merge(session);
        em.flush();
    }

    @Override
    public void revokeSession(UUID sessionId) {
        em.createQuery("DELETE UserSessions s WHERE s.session_id = :sessionId")
                .setParameter("sessionId", sessionId)
                .executeUpdate();
    }

    @Override
    public void revokeAllSessions(Integer userId) {
        em.createQuery("DELETE UserSessions s WHERE s.user_id.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public List<UserSessions> findByUser(Users user) {
        return em.createQuery("SELECT s FROM UserSessions s WHERE s.user_id = :user", UserSessions.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public UserSessions findBySessionId(UUID sessionId) {
        return em.find(UserSessions.class, sessionId);
    }

    public void updateSessionExpiration(UUID sessionId, Timestamp newExpiresAt) {
        em.createQuery("UPDATE UserSessions s SET s.expiresAt = :newExpiresAt WHERE s.session_id = :sessionId")
                .setParameter("sessionId", sessionId)
                .setParameter("newExpiresAt", newExpiresAt)
                .executeUpdate();
    }

    @Override
    public List<UserSessions> findActiveSessionsByUser(Users user) {
        return em.createQuery(
                        "SELECT s FROM UserSessions s WHERE s.user_id = :user AND s.expiresAt > CURRENT_TIMESTAMP",
                        UserSessions.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public void updateSessionAccess(UUID sessionId, Timestamp lastAccess, Timestamp newExpiresAt) {
        em.createQuery("UPDATE UserSessions s SET s.lastAccess = :lastAccess, s.expiresAt = :expiresAt WHERE s.id = :sessionId")
                .setParameter("lastAccess", lastAccess)
                .setParameter("expiresAt", newExpiresAt)
                .setParameter("sessionId", sessionId)
                .executeUpdate();
    }
}
