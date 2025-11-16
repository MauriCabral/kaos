package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Extra;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class ExtraRepository {
    public List<Extra> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Extra> query = em.createQuery(
                    "SELECT e FROM Extra e", Extra.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Extra save(Extra extra) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (extra.getId() == null) {
                em.persist(extra);
            } else {
                extra = em.merge(extra);
            }
            em.getTransaction().commit();
            return extra;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
