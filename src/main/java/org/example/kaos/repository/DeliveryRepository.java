package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Delivery;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class DeliveryRepository {

    public List<Delivery> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Delivery> query = em.createQuery(
                    "SELECT d FROM Delivery d ORDER BY d.id DESC", Delivery.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Delivery> findAllByStoreId(Long idStore) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Delivery> query = em.createQuery(
                    "SELECT d FROM Delivery d WHERE d.store.id = :idStore ORDER BY d.name",
                    Delivery.class);
            query.setParameter("idStore", idStore);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Delivery findById(Long deliveryId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(Delivery.class, deliveryId);
        } finally {
            em.close();
        }
    }

    public Delivery save(Delivery delivery) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(delivery);
            em.getTransaction().commit();
            return delivery;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Delivery update(Delivery delivery) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Delivery updatedDelivery = em.merge(delivery);
            em.getTransaction().commit();
            return updatedDelivery;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Delivery delivery = em.find(Delivery.class, id);
            if (delivery != null) {
                em.remove(delivery);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Delivery> findByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Delivery> query = em.createQuery(
                    "SELECT d FROM Delivery d WHERE LOWER(d.name) LIKE LOWER(:name) ORDER BY d.name",
                    Delivery.class);
            query.setParameter("name", "%" + name + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
