/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.primefaces.model.SortMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author jameswong
 */
@Stateless
public class NotificationsFacade extends AbstractFacade<Notifications> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NotificationsFacade() {
        super(Notifications.class);
    }

    public List<Notifications> findUnreadByUser(Users user) {
        return em.createNamedQuery("Notification.findUnreadByUser", Notifications.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void markAllAsRead(Users user) {
        em.createNamedQuery("Notification.markAllAsRead")
                .setParameter("user", user)
                .executeUpdate();
    }

    public List<Notifications> findRange(int first, int pageSize, String filter,
                                         Map<String, SortMeta> sortBy, Users user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Notifications> cq = cb.createQuery(Notifications.class);
        Root<Notifications> root = cq.from(Notifications.class);
        
        // Filtering
        Predicate filterPredicate = cb.conjunction();
        if (filter != null && !filter.isEmpty()) {
            filterPredicate = cb.or(
                cb.like(cb.lower(root.get("title")), "%" + filter.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("message")), "%" + filter.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("type").as(String.class)), "%" + filter.toLowerCase() + "%")
            );
        }
        filterPredicate = cb.and(filterPredicate, cb.equal(root.get("user"), user));
        
        cq.where(filterPredicate);
        
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

    public int count(String filter, Users user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Notifications> root = cq.from(Notifications.class);
        
        cq.select(cb.count(root));
        
        Predicate filterPredicate = cb.conjunction();
        if (filter != null && !filter.isEmpty()) {
            filterPredicate = cb.or(
                cb.like(cb.lower(root.get("title")), "%" + filter.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("message")), "%" + filter.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("type").as(String.class)), "%" + filter.toLowerCase() + "%")
            );
        }
        filterPredicate = cb.and(filterPredicate, cb.equal(root.get("user"), user));
        
        cq.where(filterPredicate);
        
        return em.createQuery(cq).getSingleResult().intValue();
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

        return cb.or(
            cb.like(cb.lower(root.get("username")), "%" + filter.toLowerCase() + "%"),
            cb.like(cb.lower(root.get("email")), "%" + filter.toLowerCase() + "%")
        );
    }   
}
