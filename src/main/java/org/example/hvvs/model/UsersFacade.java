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
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import org.primefaces.model.SortMeta;

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
    
    public List<Users> findRange(int first, int pageSize, String filter, Map<String, SortMeta> sortBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Users> cq = cb.createQuery(Users.class);
        Root<Users> root = cq.from(Users.class);
        
        // Filtering
        Predicate filterPredicate = buildFilterPredicate(cb, root, filter);
        if (filterPredicate != null) {
            cq.where(filterPredicate);
        }
        
        // Sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            List<Order> orders = sortBy.values().stream()
                .map(sort -> sort.getOrder().isAscending() ? 
                    cb.asc(root.get(sort.getField())) : 
                    cb.desc(root.get(sort.getField())))
                .collect(Collectors.toList());
            cq.orderBy(orders);
        }
        
        return em.createQuery(cq)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public int count(String filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Users> root = cq.from(Users.class);
        
        cq.select(cb.count(root));
        
        Predicate filterPredicate = buildFilterPredicate(cb, root, filter);
        if (filterPredicate != null) {
            cq.where(filterPredicate);
        }
        
        return em.createQuery(cq).getSingleResult().intValue();
    }

    private Predicate buildFilterPredicate(CriteriaBuilder cb, Root<Users> root, String filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        
        String pattern = "%" + filter.toLowerCase() + "%";
        return cb.or(
            cb.like(cb.lower(root.get("username")), pattern),
            cb.like(cb.lower(root.get("email")), pattern),
            cb.like(cb.lower(root.get("first_name")), pattern),
            cb.like(cb.lower(root.get("last_name")), pattern),
            cb.like(cb.lower(root.get("phone_number")), pattern),
            cb.like(cb.lower(root.get("role")), pattern)
        );
    }
}
