package org.example.hvvs.modules.admin.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.VisitorRecords;

import java.util.List;

@Stateless
public class VisitorRecordServiceImpl implements VisitorRecordService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<VisitorRecords> getAllVisitorRecords() {
        return entityManager.createNamedQuery("VisitorRecord.findAll", VisitorRecords.class)
                .getResultList();
    }
    
    @Override
    public VisitorRecords getVisitorRecordById(Long id) {
        return entityManager.find(VisitorRecords.class, id);
    }
    
    @Override
    public void createVisitorRecord(VisitorRecords record) {
        entityManager.persist(record);
    }
    
    @Override
    public void updateVisitorRecord(VisitorRecords record) {
        entityManager.merge(record);
    }
    
    @Override
    public void deleteVisitorRecord(VisitorRecords record) {
        entityManager.remove(entityManager.contains(record) ? record : entityManager.merge(record));
    }
} 