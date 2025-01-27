package org.example.hvvs.modules.security.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.SecurityStaffProfile;
import org.example.hvvs.model.User;

@ApplicationScoped
public class SettingsServiceSecurity {

    @PersistenceContext
    private EntityManager em;

    /**
     * Find a User by ID.
     */
    public User findUserById(Integer userId) {
        return em.find(User.class, userId);
    }

    /**
     * Update User entity.
     */
    public void updateUser(User user) {
        em.merge(user);
    }

    /**
     * Find SecurityStaffProfile by associated user ID.
     */
    public SecurityStaffProfile findSecurityStaffProfileByUserId(Integer userId) {
        return em.createNamedQuery("SecurityStaffProfile.findByUserId", SecurityStaffProfile.class)
                .setParameter("user_id", em.find(User.class, userId))
                .getSingleResult();
    }

    /**
     * Check if email exists for another user
     */
    public boolean isEmailExists(String email, Integer currentUserId) {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email AND u.id != :userId", Long.class)
                .setParameter("email", email)
                .setParameter("userId", currentUserId)
                .getSingleResult();
        return count > 0;
    }

    /**
     * Check if username exists for another user
     */
    public boolean isUsernameExists(String username, Integer currentUserId) {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.id != :userId", Long.class)
                .setParameter("username", username)
                .setParameter("userId", currentUserId)
                .getSingleResult();
        return count > 0;
    }

    /**
     * Update SecurityStaffProfile entity.
     */
    public void updateSecurityStaffProfile(SecurityStaffProfile profile) {
        em.merge(profile);
    }
}
