package org.example.hvvs.modules.resident.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.ResidentProfiles;
import org.example.hvvs.model.Users;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SettingsServiceResident {

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
     * Find ResidentProfile by associated user ID.
     */
    public ResidentProfiles findResidentProfileByUserId(Integer userId) {
        // Example approach:
        return em.createNamedQuery("ResidentProfile.findByUserId", ResidentProfiles.class)
                .setParameter("user_id", em.find(Users.class, userId))
                .getSingleResult();
    }

    /**
     * Update or merge the ResidentProfile entity.
     */
    public void updateResidentProfile(ResidentProfiles residentProfiles) {
        em.merge(residentProfiles);
    }

    /**
     * Check if a username already exists for a different user
     */
    public boolean isUsernameExists(String username, Integer currentUserId) {
        try {
            List<Users> users = em.createNamedQuery("User.findByUsername", Users.class)
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
            List<Users> users = em.createNamedQuery("User.findByEmail", Users.class)
                    .setParameter("email", email)
                    .getResultList();

            // If no users found with this email, or the only user found is the current user
            return !users.isEmpty() && !users.getFirst().getId().equals(currentUserId);
        } catch (Exception e) {
            // Log the error if needed
            return false;
        }
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
