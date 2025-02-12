/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 *
 * @author jameswong
 */
@Stateless
public class MfaMethodsFacade extends AbstractFacade<MfaMethods> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MfaMethodsFacade() {
        super(MfaMethods.class);
    }


    public List<MfaMethods> findMfaMethodsByUser(Users user) {
        TypedQuery<MfaMethods> query = em.createQuery(
                "SELECT m FROM MfaMethods m WHERE m.user = :user",
                MfaMethods.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<MfaMethods> findEnabledMfaMethodsByUser(Users user) {
        TypedQuery<MfaMethods> query = em.createQuery(
                "SELECT m FROM MfaMethods m WHERE m.user = :user AND m.isEnabled = true",
                MfaMethods.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public MfaMethods findPrimaryMfaMethodByUser(Users user) {
        try {
            return em.createQuery(
                            "SELECT m FROM MfaMethods m WHERE m.user = :user AND m.isEnabled = true AND m.isPrimary = true",
                            MfaMethods.class)
                    .setParameter("user", user)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
