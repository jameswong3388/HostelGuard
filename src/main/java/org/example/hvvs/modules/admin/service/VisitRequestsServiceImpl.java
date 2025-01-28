package org.example.hvvs.modules.admin.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.VisitRequests;

import java.sql.Timestamp;
import java.util.List;

@ApplicationScoped
public class VisitRequestsServiceImpl implements VisitRequestsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<VisitRequests> getAllRequests() {
        return entityManager.createNamedQuery("VisitRequest.findAll", VisitRequests.class)
                .getResultList();
    }

    @Override
    @Transactional
    public void updateRequest(VisitRequests request) {
        request.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        entityManager.merge(request);
    }

    @Override
    @Transactional
    public void deleteRequests(List<VisitRequests> requests) {
        for (VisitRequests request : requests) {
            VisitRequests managedRequest = entityManager.find(VisitRequests.class, request.getId());
            if (managedRequest != null) {
                entityManager.remove(managedRequest);
            }
        }
    }
} 