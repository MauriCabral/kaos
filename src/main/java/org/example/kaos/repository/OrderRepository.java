package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.Order;
import org.example.kaos.util.JpaUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")

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
            StringBuilder jpql = new StringBuilder("SELECT o FROM Order o LEFT JOIN FETCH o.store");
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
            order = em.merge(order);
            em.getTransaction().commit();
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error updating order", e);
        } finally {
            em.close();
        }
    }

    public Map<String, Integer> getBurgerSalesForDate(LocalDate date) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime nextDay = startOfDay.plusDays(1);

            Query query = em.createQuery(
                    "SELECT od.productName, od.variantName, SUM(od.quantity) " +
                            "FROM OrderDetail od " +
                            "WHERE od.productName IS NOT NULL AND od.createdAt >= :start AND od.createdAt < :end " +
                            "GROUP BY od.productName, od.variantName " +
                            "ORDER BY od.productName, od.variantName"
            );
            query.setParameter("start", startOfDay);
            query.setParameter("end", nextDay);

            List<Object[]> results = query.getResultList();
            Map<String, Integer> sales = new HashMap<>();
            for (Object[] row : results) {
                String name = (String) row[0];
                String type = (String) row[1];
                Long totalQty = (Long) row[2];
                sales.put(type != null ? name + " (" + type + ")" : name, totalQty.intValue());
            }
            return sales;
        } finally {
            em.close();
        }
    }

    public Map<String, Integer> getBurgerSalesForDateRange(LocalDate startDate, LocalDate endDate) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createQuery(
                    "SELECT od.productName, od.variantName, SUM(od.quantity) " +
                            "FROM OrderDetail od " +
                            "WHERE od.productName IS NOT NULL AND DATE(od.createdAt) BETWEEN :startDate AND :endDate " +
                            "GROUP BY od.productName, od.variantName " +
                            "ORDER BY od.productName, od.variantName"
            );
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Object[]> results = query.getResultList();
            Map<String, Integer> sales = new HashMap<>();
            for (Object[] row : results) {
                String name = (String) row[0];
                String type = (String) row[1];
                Long totalQty = (Long) row[2];
                sales.put(type != null ? name + " (" + type + ")" : name, totalQty.intValue());
            }
            return sales;
        } finally {
            em.close();
        }
    }

    public List<Map<String, Object>> getSalesPerDay(LocalDate startDate, LocalDate endDate) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createQuery(
                    "SELECT DATE(o.createdAt) as date, SUM(o.total) as total FROM Order o " +
                    "WHERE DATE(o.createdAt) BETWEEN :startDate AND :endDate " +
                    "GROUP BY DATE(o.createdAt) " +
                    "ORDER BY DATE(o.createdAt)");
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> sales = new java.util.ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("date", row[0].toString());
                map.put("total", row[1]);
                sales.add(map);
            }
            return sales;
        } finally {
            em.close();
        }
    }
}
