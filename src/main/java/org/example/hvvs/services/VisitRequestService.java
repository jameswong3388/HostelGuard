package org.example.hvvs.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.User;
import org.example.hvvs.model.VisitRequest;

import java.util.List;

@ApplicationScoped
public class VisitRequestService {
    @PersistenceContext
    private EntityManager em;

    public void create(VisitRequest request) {
        try {
            em.persist(request);
        } catch (Exception e) {
            throw e;
        }
    }

    public void update(VisitRequest request) {
        em.merge(request);
    }

    public List<VisitRequest> findRequestsByUserEntity(User user) {
        return em.createNamedQuery("VisitRequest.findByUserId", VisitRequest.class)
                .setParameter("user_id", user)
                .getResultList();
    }
} 