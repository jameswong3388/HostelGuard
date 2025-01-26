package org.example.hvvs.modules.resident.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.model.User;

@ApplicationScoped
public class SettingsService {

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
}
