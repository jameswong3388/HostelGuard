package org.example.hvvs.modules.auth.service;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.example.hvvs.model.MfaMethods;
import org.example.hvvs.model.Users;

import java.util.List;

@Stateless
public class AuthRepositoryImpl implements AuthRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
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

    @Override
    public List<MfaMethods> findMfaMethodByUser(Users user) {
        try {
            return em.createQuery(
                    "SELECT m FROM MfaMethods m WHERE m.user = :user AND m.isEnabled = true",
                    MfaMethods.class)
                .setParameter("user", user)
                .getResultList();
        } catch (Exception e) {
            return null;
        }
    }
}
