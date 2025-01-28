package org.example.hvvs.modules.admin.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.ResidentProfiles;
import org.example.hvvs.model.SecurityStaffProfiles;
import org.example.hvvs.model.ManagingStaffProfiles;
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
    public List<Users> getAllUsers() {
        TypedQuery<Users> query = entityManager.createNamedQuery("User.findAll", Users.class);
        return query.getResultList();
    }

    @Override
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
        
        entityManager.persist(user);
        return user;
    }

    @Override
    public Users updateUser(Users user) {
        // Update timestamp
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        return entityManager.merge(user);
    }

    @Override
    public void deleteUser(Users user) {
        Users managedUser = entityManager.find(Users.class, user.getId());
        if (managedUser != null) {
            entityManager.remove(managedUser);
        }
    }

    @Override
    public Users findByUsername(String username) {
        try {
            TypedQuery<Users> query = entityManager.createNamedQuery("User.findByUsername", Users.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Users findByEmail(String email) {
        try {
            TypedQuery<Users> query = entityManager.createNamedQuery("User.findByEmail", Users.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Users> findByRole(String role) {
        TypedQuery<Users> query = entityManager.createNamedQuery("User.findByRole", Users.class);
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
    public ResidentProfiles createResidentProfile(ResidentProfiles profile) {
        entityManager.persist(profile);
        return profile;
    }

    @Override
    public SecurityStaffProfiles createSecurityStaffProfile(SecurityStaffProfiles profile) {
        entityManager.persist(profile);
        return profile;
    }

    @Override
    public ManagingStaffProfiles createManagingStaffProfile(ManagingStaffProfiles profile) {
        entityManager.persist(profile);
        return profile;
    }
} 