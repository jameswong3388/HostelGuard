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
 * @author jameswong
 */
@Stateless
public class NotificationsFacade extends AbstractFacade<Notifications> {

    @PersistenceContext(unitName = "HostelGuard")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public NotificationsFacade() {
        super(Notifications.class);
    }

    public List<Notifications> findUnreadByUser(Users user) {
        return em.createNamedQuery("Notification.findUnreadByUser", Notifications.class)
                .setParameter("user", user)
                .getResultList();
    }

    public void markAllAsRead(Users user) {
        em.createNamedQuery("Notification.markAllAsRead")
                .setParameter("user", user)
                .executeUpdate();
    }
}
