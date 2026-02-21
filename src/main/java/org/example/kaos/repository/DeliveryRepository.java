package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Delivery;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeliveryRepository {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryRepository.class);

    public List<Delivery> findAll() {
        logger.debug("Buscando todas las entregas");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Delivery> query = em.createQuery(
                    "SELECT d FROM Delivery d ORDER BY d.id DESC", Delivery.class);
            List<Delivery> deliveries = query.getResultList();
            logger.debug("Se encontraron {} entregas", deliveries.size());
            return deliveries;
        } catch (Exception e) {
            logger.error("Error al buscar todas las entregas", e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public List<Delivery> findAllByStoreId(Long idStore) {
        logger.debug("Buscando entregas por ID de tienda: {}", idStore);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Delivery> query = em.createQuery(
                    "SELECT d FROM Delivery d WHERE d.store.id = :idStore ORDER BY d.name",
                    Delivery.class);
            query.setParameter("idStore", idStore);
            List<Delivery> deliveries = query.getResultList();
            logger.debug("Se encontraron {} entregas para tienda ID: {}", deliveries.size(), idStore);
            return deliveries;
        } catch (Exception e) {
            logger.error("Error al buscar entregas por ID de tienda: {}", idStore, e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public Delivery findById(Long deliveryId) {
        logger.debug("Buscando entrega por ID: {}", deliveryId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Delivery delivery = em.find(Delivery.class, deliveryId);
            if (delivery != null) {
                logger.debug("Entrega encontrada: {}", delivery.getName());
            } else {
                logger.warn("Entrega no encontrada por ID: {}", deliveryId);
            }
            return delivery;
        } catch (Exception e) {
            logger.error("Error al buscar entrega por ID: {}", deliveryId, e);
            return null;
        } finally {
            em.close();
        }
    }

    public Delivery save(Delivery delivery) {
        logger.debug("Guardando entrega: {}", delivery.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(delivery);
            em.getTransaction().commit();
            logger.info("Entrega guardada: {}", delivery.getName());
            return delivery;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al guardar entrega: {}", delivery.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public Delivery update(Delivery delivery) {
        logger.debug("Actualizando entrega: {}", delivery.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Delivery updatedDelivery = em.merge(delivery);
            em.getTransaction().commit();
            logger.info("Entrega actualizada: {}", delivery.getName());
            return updatedDelivery;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al actualizar entrega: {}", delivery.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        logger.debug("Eliminando entrega por ID: {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Delivery delivery = em.find(Delivery.class, id);
            if (delivery != null) {
                em.remove(delivery);
                logger.info("Entrega eliminada: {}", delivery.getName());
            } else {
                logger.warn("No se encontró entrega por ID: {}", id);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al eliminar entrega por ID: {}", id, e);
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Delivery> findByName(String name) {
        logger.debug("Buscando entregas por nombre: {}", name);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Delivery> query = em.createQuery(
                    "SELECT d FROM Delivery d WHERE LOWER(d.name) LIKE LOWER(:name) ORDER BY d.name",
                    Delivery.class);
            query.setParameter("name", "%" + name + "%");
            List<Delivery> deliveries = query.getResultList();
            logger.debug("Se encontraron {} entregas por nombre: {}", deliveries.size(), name);
            return deliveries;
        } catch (Exception e) {
            logger.error("Error al buscar entregas por nombre: {}", name, e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
