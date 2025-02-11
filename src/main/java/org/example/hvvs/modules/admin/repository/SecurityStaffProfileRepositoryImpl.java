package org.example.hvvs.modules.admin.repository;

import org.example.hvvs.model.SecurityStaffProfiles;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@ApplicationScoped
public class SecurityStaffProfileRepositoryImpl implements SecurityStaffProfileRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<SecurityStaffProfiles> findAll() {
        return entityManager.createQuery("SELECT s FROM SecurityStaffProfiles s", SecurityStaffProfiles.class)
                .getResultList();
    }
} 