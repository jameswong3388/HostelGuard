package org.example.hvvs.modules.admin.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.model.ManagingStaffProfiles;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.MfaMethods;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SettingsServiceAdmin {

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
     * Find ManagingStaffProfile by associated user ID.
     */
    public ManagingStaffProfiles findManagingStaffProfileByUserId(Integer userId) {
        return em.createNamedQuery("ManagingStaffProfile.findByUserId", ManagingStaffProfiles.class)
                .setParameter("user_id", em.find(Users.class, userId))
                .getSingleResult();
    }

    /**
     * Update or merge the ManagingStaffProfile entity.
     */
    public void updateManagingStaffProfile(ManagingStaffProfiles managingStaffProfiles) {
        em.merge(managingStaffProfiles);
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
