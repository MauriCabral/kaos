package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Store;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class StoreRepository {

    public Store findStoreById(Long storeId) {
        EntityManager em = JpaUtil.getEntityManager();
        Store store = null;
        try {
            TypedQuery<Store> query = em.createQuery(
                    "SELECT s FROM Store s WHERE s.id = :storeId", Store.class);
            query.setParameter("storeId", storeId);
            store = query.getSingleResult();
        } catch (NoResultException e) {
        } finally {
            em.close();
        }
        return store;
    }

    public List<Store> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Store> query = em.createQuery(
                    "SELECT s FROM Store s ORDER BY s.id", Store.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void update(Store store) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(store);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
