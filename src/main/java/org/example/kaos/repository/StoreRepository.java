package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Store;
import org.example.kaos.util.JpaUtil;

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
}
