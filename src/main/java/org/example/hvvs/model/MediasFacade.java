/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 *
 * @author jameswong
 */
@Stateless
public class MediasFacade extends AbstractFacade<Medias> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MediasFacade() {
        super(Medias.class);
    }

    public List<Medias> findByModelAndModelId(String model, String modelId) {
        TypedQuery<Medias> query = em.createQuery(
                "SELECT m FROM Medias m WHERE m.model = :model AND m.modelId = :modelId",
                Medias.class
        );
        query.setParameter("model", model);
        query.setParameter("modelId", modelId);
        return query.getResultList();
    }

    public List<Medias> findByCollection(String collection) {
        TypedQuery<Medias> query = em.createQuery(
                "SELECT m FROM Medias m WHERE m.collection = :collection",
                Medias.class
        );
        query.setParameter("collection", collection);
        List<Medias> results = query.getResultList();
        results.forEach(em::refresh);
        return results;
    }

    public void deleteByModelAndModelId(String model, String modelId) {
        try {
            em.flush();
            em.clear();
            em.createQuery(
                            "DELETE FROM Medias m WHERE m.model = :model AND m.modelId = :modelId"
                    )
                    .setParameter("model", model)
                    .setParameter("modelId", modelId)
                    .executeUpdate();
            em.flush();
        } catch (Exception e) {
            // If the entity is already deleted, we can ignore the error
            if (!e.getMessage().contains("Row was updated or deleted")) {
                throw e;
            }
        }
    }
}
