package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.Order;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class OrderRepository {
    public Order save(Order order) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (order.getId() == null) {
                em.persist(order);
                em.flush();

                String orderNumber = String.format("ORD-%04d", order.getId());
                order.setOrderNumber(orderNumber);

                order = em.merge(order);
            } else {
                order = em.merge(order);
            }

            em.getTransaction().commit();
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public Order findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        Order order = null;
        try {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT o FROM Order o WHERE o.id = :orderId", Order.class);
            query.setParameter("orderId", id);
            order = query.getSingleResult();
        } catch (NoResultException e) {
        } finally {
            em.close();
        }
        return order;
    }

    public boolean existsByOrderNumber(String orderNumber) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(o) FROM Order o WHERE o.orderNumber = :orderNumber", Long.class);
            query.setParameter("orderNumber", orderNumber);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public String getLastOrderNumber() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createQuery(
                    "SELECT MAX(o.orderNumber) FROM Order o WHERE o.orderNumber LIKE 'ORD-%'");
            String lastNumber = (String) query.getSingleResult();
            return lastNumber != null ? lastNumber : "ORD-0000";
        } finally {
            em.close();
        }
    }

    public String generateUniqueOrderNumber() {
        String baseNumber = "ORD-" + (System.currentTimeMillis() % 10000);
        String orderNumber = baseNumber;
        int attempt = 0;

        while (existsByOrderNumber(orderNumber) && attempt < 100) {
            orderNumber = baseNumber + "-" + (++attempt);
        }

        return orderNumber;
    }

    public List<Order> findAll(Boolean isAdmin) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT o FROM Order o");
            if (!isAdmin) {
                jpql.append(" WHERE o.deletedAt IS NULL");
            }
            jpql.append(" ORDER BY o.id DESC");
            TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Order update(Order order) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(order);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
        return order;
    }
}
