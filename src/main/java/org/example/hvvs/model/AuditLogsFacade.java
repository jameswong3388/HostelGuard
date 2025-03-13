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
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;
import org.primefaces.model.SortMeta;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author jameswong
 */
@Stateless
public class AuditLogsFacade extends AbstractFacade<AuditLogs> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AuditLogsFacade() {
        super(AuditLogs.class);
    }
    
    public List<AuditLogs> findRange(int first, int pageSize, String filter, Map<String, SortMeta> sortBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<AuditLogs> cq = cb.createQuery(AuditLogs.class);
        Root<AuditLogs> root = cq.from(AuditLogs.class);
        
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
        } else {
            // Default sorting by created_at DESC if no sort specified
            cq.orderBy(cb.desc(root.get("created_at")));
        }
        
        return em.createQuery(cq)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public int count(String filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<AuditLogs> root = cq.from(AuditLogs.class);
        
        cq.select(cb.count(root));
        
        Predicate filterPredicate = buildFilterPredicate(cb, root, filter);
        if (filterPredicate != null) {
            cq.where(filterPredicate);
        }
        
        return em.createQuery(cq).getSingleResult().intValue();
    }

    private Predicate buildFilterPredicate(CriteriaBuilder cb, Root<AuditLogs> root, String filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        
        String pattern = "%" + filter.toLowerCase() + "%";
        return cb.or(
            cb.like(cb.lower(root.get("action").as(String.class)), pattern),
            cb.like(cb.lower(root.get("entity_type")), pattern),
            cb.like(cb.lower(root.get("entity_id")), pattern),
            cb.like(cb.lower(root.get("description")), pattern),
            cb.like(cb.lower(root.get("ip_address")), pattern)
        );
    }
}
