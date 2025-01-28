package org.example.hvvs.modules.security.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.SecurityStaffProfiles;
import org.example.hvvs.model.Users;

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
}
