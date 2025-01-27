package org.example.hvvs.modules.admin.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.model.User;
import org.example.hvvs.model.ResidentProfile;
import org.example.hvvs.model.SecurityStaffProfile;
import org.example.hvvs.model.ManagingStaffProfile;
import org.example.hvvs.utils.DigestUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Base64;
import java.security.SecureRandom;

@Stateless
public class UserServiceImpl implements UsersService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> getAllUsers() {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findAll", User.class);
        return query.getResultList();
    }

    @Override
    public User createUser(User user) {
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
        
        entityManager.persist(user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        // Update timestamp
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        return entityManager.merge(user);
    }

    @Override
    public void deleteUser(User user) {
        User managedUser = entityManager.find(User.class, user.getId());
        if (managedUser != null) {
            entityManager.remove(managedUser);
        }
    }

    @Override
    public User findByUsername(String username) {
        try {
            TypedQuery<User> query = entityManager.createNamedQuery("User.findByUsername", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            TypedQuery<User> query = entityManager.createNamedQuery("User.findByEmail", User.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<User> findByRole(String role) {
        TypedQuery<User> query = entityManager.createNamedQuery("User.findByRole", User.class);
        query.setParameter("role", role);
        return query.getResultList();
    }

    @Override
    public boolean isUsernameExists(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public boolean isEmailExists(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public ResidentProfile createResidentProfile(ResidentProfile profile) {
        entityManager.persist(profile);
        return profile;
    }

    @Override
    public SecurityStaffProfile createSecurityStaffProfile(SecurityStaffProfile profile) {
        entityManager.persist(profile);
        return profile;
    }

    @Override
    public ManagingStaffProfile createManagingStaffProfile(ManagingStaffProfile profile) {
        entityManager.persist(profile);
        return profile;
    }
} 