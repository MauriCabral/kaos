package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Store;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StoreRepository {
    private static final Logger logger = LoggerFactory.getLogger(StoreRepository.class);

    public Store findStoreById(Long storeId) {
        logger.debug("Buscando tienda por ID: {}", storeId);
        EntityManager em = JpaUtil.getEntityManager();
        Store store = null;
        try {
            TypedQuery<Store> query = em.createQuery(
                    "SELECT s FROM Store s WHERE s.id = :storeId", Store.class);
            query.setParameter("storeId", storeId);
            store = query.getSingleResult();
            logger.debug("Tienda encontrada: {}", store.getName());
        } catch (NoResultException e) {
            logger.warn("Tienda no encontrada por ID: {}", storeId);
        } catch (Exception e) {
            logger.error("Error al buscar tienda por ID: {}", storeId, e);
        } finally {
            em.close();
        }
        return store;
    }

    public List<Store> findAll() {
        logger.debug("Buscando todas las tiendas");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Store> query = em.createQuery(
                    "SELECT s FROM Store s ORDER BY s.id", Store.class);
            List<Store> stores = query.getResultList();
            logger.debug("Se encontraron {} tiendas", stores.size());
            return stores;
        } catch (Exception e) {
            logger.error("Error al buscar todas las tiendas", e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public void update(Store store) {
        logger.debug("Actualizando tienda: {}", store.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(store);
            em.getTransaction().commit();
            logger.info("Tienda actualizada: {}", store.getName());
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al actualizar tienda: {}", e.getMessage(), e);
            }
            logger.error("Error al actualizar tienda: {}", store.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }
}
