/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author jameswong
 */
@Stateless
public class UserSessionsFacade extends AbstractFacade<UserSessions> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UserSessionsFacade() {
        super(UserSessions.class);
    }

    public void revokeSession(UUID sessionId) {
        em.createQuery("DELETE UserSessions s WHERE s.session_id = :sessionId")
                .setParameter("sessionId", sessionId)
                .executeUpdate();
    }

    public void revokeAllSessions(Integer userId) {
        em.createQuery("DELETE UserSessions s WHERE s.user_id.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    public List<UserSessions> findActiveSessionsByUser(Users user) {
        return em.createQuery(
                        "SELECT s FROM UserSessions s WHERE s.user_id = :user AND s.expiresAt > CURRENT_TIMESTAMP",
                        UserSessions.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void updateSessionAccess(UUID sessionId, Timestamp lastAccess, Timestamp newExpiresAt) {
        em.createQuery("UPDATE UserSessions s SET s.lastAccess = :lastAccess, s.expiresAt = :expiresAt WHERE s.id = :sessionId")
                .setParameter("lastAccess", lastAccess)
                .setParameter("expiresAt", newExpiresAt)
                .setParameter("sessionId", sessionId)
                .executeUpdate();
    }

    public void updateSessionExpiration(UUID sessionId, Timestamp newExpiresAt) {
        em.createQuery("UPDATE UserSessions s SET s.expiresAt = :newExpiresAt WHERE s.session_id = :sessionId")
                .setParameter("sessionId", sessionId)
                .setParameter("newExpiresAt", newExpiresAt)
                .executeUpdate();
    }
    
}
