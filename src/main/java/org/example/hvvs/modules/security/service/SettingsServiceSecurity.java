package org.example.hvvs.modules.security.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.SecurityStaffProfiles;
import org.example.hvvs.model.Users;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SettingsServiceSecurity {

    @PersistenceContext
    private EntityManager em;

    /**
     * Find a User by ID.
     */
    public Users findUserById(Integer userId) {
        return em.find(Users.class, userId);
    }

    /**
     * Update User entity.
     */
    public void updateUser(Users user) {
        em.merge(user);
    }

    /**
     * Find SecurityStaffProfile by associated user ID.
     */
    public SecurityStaffProfiles findSecurityStaffProfileByUserId(Integer userId) {
        return em.createNamedQuery("SecurityStaffProfile.findByUserId", SecurityStaffProfiles.class)
                .setParameter("user_id", em.find(Users.class, userId))
                .getSingleResult();
    }

    /**
     * Check if email exists for another user
     */
    public boolean isEmailExists(String email, Integer currentUserId) {
        Long count = em.createQuery("SELECT COUNT(u) FROM Users u WHERE u.email = :email AND u.id != :userId", Long.class)
                .setParameter("email", email)
                .setParameter("userId", currentUserId)
                .getSingleResult();
        return count > 0;
    }

    /**
     * Check if username exists for another user
     */
    public boolean isUsernameExists(String username, Integer currentUserId) {
        Long count = em.createQuery("SELECT COUNT(u) FROM Users u WHERE u.username = :username AND u.id != :userId", Long.class)
                .setParameter("username", username)
                .setParameter("userId", currentUserId)
                .getSingleResult();
        return count > 0;
    }

    /**
     * Update SecurityStaffProfile entity.
     */
    public void updateSecurityStaffProfile(SecurityStaffProfiles profile) {
        em.merge(profile);
    }

    public void saveMfaMethod(MfaMethods mfaMethod) {
        em.persist(mfaMethod);
    }

    public MfaMethods findMfaMethodById(UUID id) {
        return em.find(MfaMethods.class, id);
    }

    @Transactional
    public void updateMfaMethod(MfaMethods mfaMethod) {
        em.merge(mfaMethod);
    }

    public void deleteMfaMethod(MfaMethods mfaMethod) {
        em.remove(mfaMethod);
    }

    public List<MfaMethods> findMfaMethodsByUser(Users user) {
        TypedQuery<MfaMethods> query = em.createQuery(
                "SELECT m FROM MfaMethods m WHERE m.user = :user AND m.isEnabled = true",
                MfaMethods.class);
        query.setParameter("user", user);
        return query.getResultList();
    }
}
