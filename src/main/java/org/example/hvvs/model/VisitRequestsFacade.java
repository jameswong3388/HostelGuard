/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

/**
 *
 * @author jameswong
 */
@Stateless
public class VisitRequestsFacade extends AbstractFacade<VisitRequests> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public VisitRequestsFacade() {
        super(VisitRequests.class);
    }

    public List<VisitRequests> findAllRequestsByUser(Users user) {
        return em.createNamedQuery("VisitRequest.findByUserId", VisitRequests.class)
                .setParameter("user_id", user)
                .getResultList();
    }
    
}
