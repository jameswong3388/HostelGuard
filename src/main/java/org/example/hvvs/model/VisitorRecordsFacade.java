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
public class VisitorRecordsFacade extends AbstractFacade<VisitorRecords> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public VisitorRecordsFacade() {
        super(VisitorRecords.class);
    }
    
    public List<VisitorRecords> findRange(int first, int pageSize, String filter, Map<String, SortMeta> sortBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<VisitorRecords> cq = cb.createQuery(VisitorRecords.class);
        Root<VisitorRecords> root = cq.from(VisitorRecords.class);
        
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
        Root<VisitorRecords> root = cq.from(VisitorRecords.class);
        
        cq.select(cb.count(root));
        
        Predicate filterPredicate = buildFilterPredicate(cb, root, filter);
        if (filterPredicate != null) {
            cq.where(filterPredicate);
        }
        
        return em.createQuery(cq).getSingleResult().intValue();
    }

    private Predicate buildFilterPredicate(CriteriaBuilder cb, Root<VisitorRecords> root, String filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }
        
        String pattern = "%" + filter.toLowerCase() + "%";
        return cb.or(
            cb.like(cb.lower(root.get("visitor_name")), pattern),
            cb.like(cb.lower(root.get("visitor_ic")), pattern),
            cb.like(cb.lower(root.get("visitor_phone")), pattern),
            cb.like(cb.lower(root.join("security_staff_id").get("username")), pattern),
            cb.like(cb.lower(root.join("request_id").get("unitNumber")), pattern)
        );
    }
}
