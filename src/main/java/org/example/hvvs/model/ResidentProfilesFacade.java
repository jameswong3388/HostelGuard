/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author jameswong
 */
@Stateless
public class ResidentProfilesFacade extends AbstractFacade<ResidentProfiles> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ResidentProfilesFacade() {
        super(ResidentProfiles.class);
    }
    
}
