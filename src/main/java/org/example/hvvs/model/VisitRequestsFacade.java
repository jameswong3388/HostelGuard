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
 *
 * @author jameswong
 */
@Stateless
public class VisitRequestsFacade extends AbstractFacade<VisitRequests> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public VisitRequestsFacade() {
        super(VisitRequests.class);
    }

    public List<VisitRequests> findAllRequestsByUser(Users user) {
        return em.createNamedQuery("VisitRequest.findByUserId", VisitRequests.class)
                .setParameter("user_id", user)
                .getResultList();
    }

    public List<VisitRequests> findRange(int first, int pageSize, String filter, Map<String, SortMeta> sortBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<VisitRequests> cq = cb.createQuery(VisitRequests.class);
        Root<VisitRequests> root = cq.from(VisitRequests.class);
        
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

    public List<VisitRequests> findRange(int first, int pageSize, String filter, Map<String, SortMeta> sortBy, Users user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<VisitRequests> cq = cb.createQuery(VisitRequests.class);
        Root<VisitRequests> root = cq.from(VisitRequests.class);
        
        // Filtering
        Predicate filterPredicate = buildFilterPredicate(cb, root, filter);
        if (user != null) {
            Predicate userPredicate = cb.equal(root.get("user_id"), user);
            if (filterPredicate != null) {
                cq.where(filterPredicate, userPredicate);
            } else {
                cq.where(userPredicate);
            }
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
        Root<VisitRequests> root = cq.from(VisitRequests.class);
        
        cq.select(cb.count(root));
        
        Predicate filterPredicate = buildFilterPredicate(cb, root, filter);
        if (filterPredicate != null) {
            cq.where(filterPredicate);
        }
        
        return em.createQuery(cq).getSingleResult().intValue();
    }

    private Predicate buildFilterPredicate(CriteriaBuilder cb, Root<VisitRequests> root, String filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        
        String pattern = "%" + filter.toLowerCase() + "%";
        return cb.or(
            cb.like(cb.lower(root.get("verification_code")), pattern),
            cb.like(cb.lower(root.get("unit_number")), pattern),
            cb.like(cb.lower(root.get("purpose")), pattern),
            cb.like(cb.lower(root.get("status")), pattern),
            cb.like(cb.lower(root.get("remarks")), pattern)
        );
    }
}
