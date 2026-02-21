package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.Order;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")

public class OrderRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderRepository.class);
    public Order save(Order order) {
        logger.debug("Guardando pedido: {}", order);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (order.getId() == null) {
                em.persist(order);
                em.flush();
                logger.info("Pedido persistido con ID: {}", order.getId());

                String orderNumber = String.format("ORD-%04d", order.getId());
                order.setOrderNumber(orderNumber);
                logger.debug("Número de pedido generado: {}", orderNumber);

                order = em.merge(order);
            } else {
                order = em.merge(order);
                logger.info("Pedido actualizado con ID: {}", order.getId());
            }

            em.getTransaction().commit();
            logger.debug("Transacción completada");
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al guardar pedido: {}", e.getMessage(), e);
            }
            logger.error("Error al guardar pedido: {}", e.getMessage(), e);
            throw e;
        } finally {
            em.close();
            logger.debug("EntityManager cerrado");
        }
    }

    public Order findById(Long id) {
        logger.debug("Buscando pedido por ID: {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        Order order = null;
        try {
            TypedQuery<Order> query = em.createQuery(
                    "SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails WHERE o.id = :orderId", Order.class);
            query.setParameter("orderId", id);
            order = query.getSingleResult();
            logger.debug("Pedido encontrado por ID: {}", id);
            
            // Force initialization of collections before session closes
            if (order != null) {
                order.getOrderDetails().size();
                for (var od : order.getOrderDetails()) {
                    od.getOrderDetailToppings().size();
                }
            }
        } catch (NoResultException e) {
            logger.warn("No se encontró pedido con ID: {}", id);
        } catch (Exception e) {
            logger.error("Error al buscar pedido por ID: {}", id, e);
        } finally {
            em.close();
        }
        return order;
    }

    public boolean existsByOrderNumber(String orderNumber) {
        logger.debug("Verificando existencia de pedido por número: {}", orderNumber);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(o) FROM Order o WHERE o.orderNumber = :orderNumber", Long.class);
            query.setParameter("orderNumber", orderNumber);
            boolean exists = query.getSingleResult() > 0;
            logger.debug("Pedido con número {} {}", orderNumber, exists ? "existe" : "no existe");
            return exists;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de pedido por número: {}", orderNumber, e);
            return false;
        } finally {
            em.close();
        }
    }

    public String getLastOrderNumber() {
        logger.debug("Obteniendo último número de pedido");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Query query = em.createQuery(
                    "SELECT MAX(o.orderNumber) FROM Order o WHERE o.orderNumber LIKE 'ORD-%'");
            String lastNumber = (String) query.getSingleResult();
            lastNumber = lastNumber != null ? lastNumber : "ORD-0000";
            logger.debug("Último número de pedido: {}", lastNumber);
            return lastNumber;
        } catch (Exception e) {
            logger.error("Error al obtener último número de pedido: {}", e.getMessage(), e);
            return "ORD-0000";
        } finally {
            em.close();
        }
    }

    public String generateUniqueOrderNumber() {
        logger.debug("Generando número de pedido único");
        String baseNumber = "ORD-" + (System.currentTimeMillis() % 10000);
        String orderNumber = baseNumber;
        int attempt = 0;

        while (existsByOrderNumber(orderNumber) && attempt < 100) {
            orderNumber = baseNumber + "-" + (++attempt);
            logger.debug("Intentando número de pedido: {}", orderNumber);
        }

        if (attempt >= 100) {
            logger.error("No se pudo generar un número de pedido único después de 100 intentos");
        }

        logger.debug("Número de pedido único generado: {}", orderNumber);
        return orderNumber;
    }

    public List<Order> findAll(Boolean isAdmin, int storeId) {
        logger.debug("Buscando todos los pedidos - isAdmin: {}, storeId: {}", isAdmin, storeId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT o FROM Order o LEFT JOIN FETCH o.store");
            List<String> conditions = new ArrayList<>();
            if (!isAdmin) {
                conditions.add("o.deletedAt IS NULL");
            }
            if (storeId > 0) {
                conditions.add("o.store.id = :storeId");
            }
            if (!conditions.isEmpty()) {
                jpql.append(" WHERE ").append(String.join(" AND ", conditions));
            }
            jpql.append(" ORDER BY o.id DESC");
            TypedQuery<Order> query = em.createQuery(jpql.toString(), Order.class);
            if (storeId > 0) {
                query.setParameter("storeId", storeId);
            }
            List<Order> orders = query.getResultList();
            logger.debug("Se encontraron {} pedidos", orders.size());
            return orders;
        } catch (Exception e) {
            logger.error("Error al buscar todos los pedidos - isAdmin: {}, storeId: {}", isAdmin, storeId, e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Order update(Order order) {
        logger.debug("Actualizando pedido: {}", order);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            order = em.merge(order);
            em.getTransaction().commit();
            logger.info("Pedido actualizado con ID: {}", order.getId());
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al actualizar pedido: {}", e.getMessage(), e);
            }
            logger.error("Error al actualizar pedido: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating order", e);
        } finally {
            em.close();
        }
    }

    public Map<String, Integer> getBurgerSalesForDate(LocalDate date) {
        return getBurgerSalesForDate(date, null);
    }

    public Map<String, Integer> getBurgerSalesForDate(LocalDate date, Long storeId) {
        logger.debug("Obteniendo ventas de hamburguesas para fecha: {}, storeId: {}", date, storeId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            LocalDateTime startOfDay = date.atTime(3, 0);
            LocalDateTime nextDay = startOfDay.plusDays(1);

            String jpql = "SELECT od.productName, od.variantName, SUM(od.quantity) " +
                    "FROM OrderDetail od JOIN od.order o " +
                    "WHERE od.productName IS NOT NULL AND od.createdAt >= :start AND od.createdAt < :end AND o.deletedAt IS NULL";
            if (storeId != null) {
                jpql += " AND o.store.id = :storeId";
            }
            jpql += " GROUP BY od.productName, od.variantName " +
                    "ORDER BY od.productName, od.variantName";

            Query query = em.createQuery(jpql);
            query.setParameter("start", startOfDay);
            query.setParameter("end", nextDay);
            if (storeId != null) {
                query.setParameter("storeId", storeId);
            }

            List<Object[]> results = query.getResultList();
            Map<String, Integer> sales = new HashMap<>();
            for (Object[] row : results) {
                String name = (String) row[0];
                String type = (String) row[1];
                Long totalQty = (Long) row[2];
                sales.put(type != null ? name + " (" + type + ")" : name, totalQty.intValue());
            }
            logger.debug("Se encontraron {} registros de ventas para la fecha: {}", sales.size(), date);
            return sales;
        } catch (Exception e) {
            logger.error("Error al obtener ventas de hamburguesas para fecha: {}, storeId: {}", date, storeId, e);
            return new HashMap<>();
        } finally {
            em.close();
        }
    }

    public Map<String, Integer> getBurgerSalesForDateRange(LocalDate startDate, LocalDate endDate) {
        return getBurgerSalesForDateRange(startDate, endDate, null);
    }

    public Map<String, Integer> getBurgerSalesForDateRange(LocalDate startDate, LocalDate endDate, Long storeId) {
        logger.debug("Obteniendo ventas de hamburguesas para rango de fechas: {} - {}, storeId: {}", startDate, endDate, storeId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT od.productName, od.variantName, SUM(od.quantity) " +
                    "FROM OrderDetail od JOIN od.order o " +
                    "WHERE od.productName IS NOT NULL AND DATE(od.createdAt) BETWEEN :startDate AND :endDate AND o.deletedAt IS NULL";
            if (storeId != null) {
                jpql += " AND o.store.id = :storeId";
            }
            jpql += " GROUP BY od.productName, od.variantName " +
                    "ORDER BY od.productName, od.variantName";

            Query query = em.createQuery(jpql);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            if (storeId != null) {
                query.setParameter("storeId", storeId);
            }
            List<Object[]> results = query.getResultList();
            Map<String, Integer> sales = new HashMap<>();
            for (Object[] row : results) {
                String name = (String) row[0];
                String type = (String) row[1];
                Long totalQty = (Long) row[2];
                sales.put(type != null ? name + " (" + type + ")" : name, totalQty.intValue());
            }
            logger.debug("Se encontraron {} registros de ventas para el rango de fechas: {} - {}", sales.size(), startDate, endDate);
            return sales;
        } catch (Exception e) {
            logger.error("Error al obtener ventas de hamburguesas para rango de fechas: {} - {}, storeId: {}", startDate, endDate, storeId, e);
            return new HashMap<>();
        } finally {
            em.close();
        }
    }

    public List<Map<String, Object>> getSalesPerDay(LocalDate startDate, LocalDate endDate) {
        return getSalesPerDay(startDate, endDate, null);
    }

    public List<Map<String, Object>> getSalesPerDay(LocalDate startDate, LocalDate endDate, Long storeId) {
        logger.debug("Obteniendo ventas por día para rango de fechas: {} - {}, storeId: {}", startDate, endDate, storeId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT DATE(o.createdAt) as date, SUM(o.total) as total FROM Order o " +
                    "WHERE DATE(o.createdAt) BETWEEN :startDate AND :endDate AND o.deletedAt IS NULL";
            if (storeId != null) {
                jpql += " AND o.store.id = :storeId";
            }
            jpql += " GROUP BY DATE(o.createdAt) " +
                    "ORDER BY DATE(o.createdAt)";

            Query query = em.createQuery(jpql);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
            if (storeId != null) {
                query.setParameter("storeId", storeId);
            }
            List<Object[]> results = query.getResultList();
            List<Map<String, Object>> sales = new java.util.ArrayList<>();
            for (Object[] row : results) {
                Map<String, Object> map = new java.util.HashMap<>();
                map.put("date", row[0].toString());
                map.put("total", row[1]);
                sales.add(map);
            }
            logger.debug("Se encontraron {} registros de ventas por día para el rango de fechas: {} - {}", sales.size(), startDate, endDate);
            return sales;
        } catch (Exception e) {
            logger.error("Error al obtener ventas por día para rango de fechas: {} - {}, storeId: {}", startDate, endDate, storeId, e);
            return new java.util.ArrayList<>();
        } finally {
            em.close();
        }
    }
}
