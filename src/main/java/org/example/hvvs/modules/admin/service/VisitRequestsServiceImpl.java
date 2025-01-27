package org.example.hvvs.modules.admin.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.VisitRequest;

import java.sql.Timestamp;
import java.util.List;

@ApplicationScoped
public class VisitRequestsServiceImpl implements VisitRequestsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<VisitRequest> getAllRequests() {
        return entityManager.createNamedQuery("VisitRequest.findAll", VisitRequest.class)
                .getResultList();
    }

    @Override
    @Transactional
    public void updateRequest(VisitRequest request) {
        request.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        entityManager.merge(request);
    }

    @Override
    @Transactional
    public void deleteRequests(List<VisitRequest> requests) {
        for (VisitRequest request : requests) {
            VisitRequest managedRequest = entityManager.find(VisitRequest.class, request.getId());
            if (managedRequest != null) {
                entityManager.remove(managedRequest);
            }
        }
    }
} 