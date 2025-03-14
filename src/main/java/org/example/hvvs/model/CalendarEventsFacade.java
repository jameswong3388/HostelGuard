/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.example.hvvs.model;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author jameswong
 */
@Stateless
public class CalendarEventsFacade extends AbstractFacade<CalendarEvents> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CalendarEventsFacade() {
        super(CalendarEvents.class);
    }
    
    /**
     * Executes a named query with a single parameter
     * 
     * @param queryName the name of the named query
     * @param paramName the parameter name
     * @param paramValue the parameter value
     * @return list of calendar events matching the query
     */
    public List<CalendarEvents> findByNamedQuery(String queryName, String paramName, Object paramValue) {
        TypedQuery<CalendarEvents> query = em.createNamedQuery(queryName, CalendarEvents.class);
        query.setParameter(paramName, paramValue);
        return query.getResultList();
    }
    
    /**
     * Finds events for a specific user
     * 
     * @param user the user to find events for
     * @return list of calendar events for the user
     */
    public List<CalendarEvents> findByUser(Users user) {
        return findByNamedQuery("CalendarEvents.findByUser", "user", user);
    }
    
}
