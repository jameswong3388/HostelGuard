package org.example.hvvs.modules.common.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.model.UserSessions;
import org.example.hvvs.model.Users;

import java.util.List;
import java.util.Optional;
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

    @Override
    public void updateLastAccess(UUID sessionId) {
        em.createQuery("UPDATE UserSessions s SET s.lastAccess = CURRENT_TIMESTAMP WHERE s.session_id = :sessionId")
                .setParameter("sessionId", sessionId)
                .executeUpdate();
    }

    @Override
    public List<UserSessions> findActiveSessionsByUser(Users user) {
        return em.createQuery(
                        "SELECT s FROM UserSessions s WHERE s.user_id = :user AND s.active = true",
                        UserSessions.class)
                .setParameter("user", user)
                .getResultList();
    }
}
