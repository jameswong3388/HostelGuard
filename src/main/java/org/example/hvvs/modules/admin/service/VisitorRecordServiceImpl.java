package org.example.hvvs.modules.admin.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.VisitorRecord;
import java.util.List;

@Stateless
public class VisitorRecordServiceImpl implements VisitorRecordService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<VisitorRecord> getAllVisitorRecords() {
        return entityManager.createNamedQuery("VisitorRecord.findAll", VisitorRecord.class)
                .getResultList();
    }
    
    @Override
    public VisitorRecord getVisitorRecordById(Long id) {
        return entityManager.find(VisitorRecord.class, id);
    }
    
    @Override
    public void createVisitorRecord(VisitorRecord record) {
        entityManager.persist(record);
    }
    
    @Override
    public void updateVisitorRecord(VisitorRecord record) {
        entityManager.merge(record);
    }
    
    @Override
    public void deleteVisitorRecord(VisitorRecord record) {
        entityManager.remove(entityManager.contains(record) ? record : entityManager.merge(record));
    }
} 