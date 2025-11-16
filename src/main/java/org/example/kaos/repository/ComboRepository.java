package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Combo;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class ComboRepository {
    public List<Combo> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Combo> query = em.createQuery(
                    "SELECT c FROM Combo c", Combo.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Combo save(Combo combo) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (combo.getId() == null) {
                em.persist(combo);
            } else {
                combo = em.merge(combo);
            }
            em.getTransaction().commit();
            return combo;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
