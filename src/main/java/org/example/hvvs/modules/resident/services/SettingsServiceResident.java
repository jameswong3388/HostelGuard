package org.example.hvvs.modules.resident.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.model.User;
import java.util.List;

@ApplicationScoped
public class SettingsServiceResident {

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
     * Find ResidentProfile by associated user ID.
     */
    public ResidentProfile findResidentProfileByUserId(Integer userId) {
        // Example approach:
        return em.createNamedQuery("ResidentProfile.findByUserId", ResidentProfile.class)
                .setParameter("user_id", em.find(User.class, userId))
                .getSingleResult();
    }

    /**
     * Update or merge the ResidentProfile entity.
     */
    public void updateResidentProfile(ResidentProfile residentProfile) {
        em.merge(residentProfile);
    }

    /**
     * Check if a username already exists for a different user
     */
    public boolean isUsernameExists(String username, Integer currentUserId) {
        try {
            List<User> users = em.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username)
                    .getResultList();

            // If no users found with this username, or the only user found is the current user
            return !users.isEmpty() && !users.getFirst().getId().equals(currentUserId);
        } catch (Exception e) {
            // Log the error if needed
            return false;
        }
    }

    /**
     * Check if an email already exists for a different user
     */
    public boolean isEmailExists(String email, Integer currentUserId) {
        try {
            List<User> users = em.createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getResultList();

            // If no users found with this email, or the only user found is the current user
            return !users.isEmpty() && !users.getFirst().getId().equals(currentUserId);
        } catch (Exception e) {
            // Log the error if needed
            return false;
        }
    }
}
