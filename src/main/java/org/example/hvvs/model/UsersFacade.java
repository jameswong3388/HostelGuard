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
import org.example.hvvs.utils.DigestUtils;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 *
 * @author jameswong
 */
@Stateless
public class UsersFacade extends AbstractFacade<Users> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsersFacade() {
        super(Users.class);
    }


    public Users createUser(Users user) {
        // Generate salt using SecureRandom
        SecureRandom random = new SecureRandom();
        byte[] saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        String salt = Base64.getEncoder().encodeToString(saltBytes);

        // Hash password using DigestUtils
        String hashedPassword = DigestUtils.sha256Digest(salt + user.getPassword());

        // Set the hashed password and salt
        user.setPassword(hashedPassword);
        user.setSalt(salt);

        // Set timestamps
        Timestamp now = Timestamp.from(Instant.now());
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Set default active status if not set
        if (!user.getIsActive()) {
            user.setIsActive(true);
        }

        this.create(user);
        return user;
    }

    public Users findByEmail(String email) {
        try {
            TypedQuery<Users> query = em.createNamedQuery("User.findByEmail", Users.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Users findByUsername(String username) {
        try {
            TypedQuery<Users> query = em.createNamedQuery("User.findByUsername", Users.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Check if a username already exists for a different user
     */
    public boolean isUsernameExists(String username) {
        try {
            List<Users> users = em.createNamedQuery("User.findByUsername", Users.class)
                    .setParameter("username", username)
                    .getResultList();

            // If no users found with this username, or the only user found is the current user
            return !users.isEmpty();
        } catch (Exception e) {
            // Log the error if needed
            return false;
        }
    }

    /**
     * Check if an email already exists for a different user
     */
    public boolean isEmailExists(String email) {
        try {
            List<Users> users = em.createNamedQuery("User.findByEmail", Users.class)
                    .setParameter("email", email)
                    .getResultList();

            // If no users found with this email, or the only user found is the current user
            return !users.isEmpty();
        } catch (Exception e) {
            // Log the error if needed
            return false;
        }
    }
    
}
