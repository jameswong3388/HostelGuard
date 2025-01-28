package org.example.hvvs.modules.resident.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.Users;
import org.example.hvvs.model.VisitRequests;

import java.util.List;

@ApplicationScoped
public class VisitRequestService {
    @PersistenceContext
    private EntityManager em;

    public void create(VisitRequests request) {
        try {
            em.persist(request);
        } catch (Exception e) {
            throw e;
        }
    }

    public void update(VisitRequests request) {
        em.merge(request);
    }

    public List<VisitRequests> findRequestsByUserEntity(Users user) {
        return em.createNamedQuery("VisitRequest.findByUserId", VisitRequests.class)
                .setParameter("user_id", user)
                .getResultList();
    }
} 