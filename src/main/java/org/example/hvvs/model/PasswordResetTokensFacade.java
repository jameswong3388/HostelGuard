/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.sql.Timestamp;
import java.util.List;

@Stateless
public class PasswordResetTokensFacade extends AbstractFacade<PasswordResetTokens> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PasswordResetTokensFacade() {
        super(PasswordResetTokens.class);
    }

    /**
     * Find all tokens for a specific user
     *
     * @param user the user to find tokens for
     * @return list of password reset tokens
     */
    public List<PasswordResetTokens> findTokensByUser(Users user) {
        TypedQuery<PasswordResetTokens> query = em.createQuery(
                "SELECT p FROM PasswordResetTokens p WHERE p.user = :user",
                PasswordResetTokens.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    /**
     * Find a token by its token string value
     *
     * @param token the token string to find
     * @return the token entity or null if not found
     */
    public PasswordResetTokens findByToken(String token) {
        try {
            return em.createNamedQuery("PasswordResetToken.findByToken", PasswordResetTokens.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Find a valid token (not used and not expired)
     *
     * @param token the token string to validate
     * @return the valid token or null if invalid or not found
     */
    public PasswordResetTokens findValidToken(String token) {
        try {
            return em.createNamedQuery("PasswordResetToken.findValidToken", PasswordResetTokens.class)
                    .setParameter("token", token)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Invalidate all existing tokens for a user
     *
     * @param user the user whose tokens should be invalidated
     */
    public void invalidateAllUserTokens(Users user) {
        em.createQuery("UPDATE PasswordResetTokens p SET p.used = true WHERE p.user = :user AND p.used = false")
                .setParameter("user", user)
                .executeUpdate();
    }

    /**
     * Mark a token as used
     *
     * @param token the token to mark as used
     */
    public void markTokenAsUsed(PasswordResetTokens token) {
        token.setUsed(true);
        em.merge(token);
    }

    /**
     * Delete expired tokens to clean up the database
     *
     * @return the number of tokens deleted
     */
    public int deleteExpiredTokens() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return em.createQuery("DELETE FROM PasswordResetTokens p WHERE p.expiresAt < :now OR p.used = true")
                .setParameter("now", now)
                .executeUpdate();
    }
} 