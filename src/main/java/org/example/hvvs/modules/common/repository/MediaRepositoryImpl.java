package org.example.hvvs.modules.common.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.example.hvvs.model.Medias;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MediaRepositoryImpl implements MediaRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Medias save(Medias media) {
        if (media.getId() == null) {
            entityManager.persist(media);
            return media;
        } else {
            return entityManager.merge(media);
        }
    }

    @Override
    public Optional<Medias> findById(UUID id) {
        Medias media = entityManager.find(Medias.class, id);
        if (media != null) {
            entityManager.refresh(media);
        }
        return Optional.ofNullable(media);
    }

    @Override
    public List<Medias> findByModelAndModelId(String model, String modelId) {
        TypedQuery<Medias> query = entityManager.createQuery(
                "SELECT m FROM Medias m WHERE m.model = :model AND m.modelId = :modelId",
                Medias.class
        );
        query.setParameter("model", model);
        query.setParameter("modelId", modelId);
        return query.getResultList();
    }

    @Override
    public List<Medias> findByCollection(String collection) {
        TypedQuery<Medias> query = entityManager.createQuery(
            "SELECT m FROM Medias m WHERE m.collection = :collection",
            Medias.class
        );
        query.setParameter("collection", collection);
        List<Medias> results = query.getResultList();
        results.forEach(entityManager::refresh);
        return results;
    }

    @Override
    @Transactional
    public void delete(Medias media) {
        try {
            entityManager.flush();
            entityManager.clear();
            if (!entityManager.contains(media)) {
                media = entityManager.merge(media);
            }
            entityManager.refresh(media);
            entityManager.remove(media);
            entityManager.flush();
        } catch (Exception e) {
            // If the entity is already deleted, we can ignore the error
            if (!e.getMessage().contains("Row was updated or deleted")) {
                throw e;
            }
        }
    }

    @Override
    @Transactional
    public void deleteByModelAndModelId(String model, String modelId) {
        try {
            entityManager.flush();
            entityManager.clear();
            entityManager.createQuery(
                "DELETE FROM Medias m WHERE m.model = :model AND m.modelId = :modelId"
            )
            .setParameter("model", model)
            .setParameter("modelId", modelId)
            .executeUpdate();
            entityManager.flush();
        } catch (Exception e) {
            // If the entity is already deleted, we can ignore the error
            if (!e.getMessage().contains("Row was updated or deleted")) {
                throw e;
            }
        }
    }
} 