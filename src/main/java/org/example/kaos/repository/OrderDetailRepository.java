package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.OrderDetail;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

import java.util.List;

public class OrderDetailRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailRepository.class);

    public OrderDetail save(OrderDetail orderDetail) {
        logger.debug("Guardando detalle de pedido: {}", orderDetail.getProductName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (orderDetail.getId() == null) {
                em.persist(orderDetail);
                logger.info("Detalle de pedido creado: {}", orderDetail.getProductName());
            } else {
                orderDetail = em.merge(orderDetail);
                logger.info("Detalle de pedido actualizado: {}", orderDetail.getProductName());
            }

            em.getTransaction().commit();
            return orderDetail;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al guardar detalle de pedido: {}", e.getMessage(), e);
            }
            logger.error("Error al guardar detalle de pedido: {}", orderDetail.getProductName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetail> saveAll(List<OrderDetail> orderDetails) {
        logger.debug("Guardando {} detalles de pedido", orderDetails.size());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            for (OrderDetail detail : orderDetails) {
                if (detail.getId() == null) {
                    em.persist(detail);
                    logger.debug("Detalle de pedido persistido: {}", detail.getProductName());
                } else {
                    em.merge(detail);
                    logger.debug("Detalle de pedido mergeado: {}", detail.getProductName());
                }
            }

            em.getTransaction().commit();
            logger.info("{} detalles de pedido guardados exitosamente", orderDetails.size());
            return orderDetails;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al guardar detalles de pedido: {}", e.getMessage(), e);
            }
            logger.error("Error al guardar detalles de pedido: {}", e.getMessage(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetail> findByOrderId(Long orderId) {
        logger.debug("Buscando detalles de pedido para pedido ID: {}", orderId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<OrderDetail> query = em.createQuery(
                    "SELECT od FROM OrderDetail od WHERE od.order.id = :orderId", OrderDetail.class);
            query.setParameter("orderId", orderId);
            List<OrderDetail> orderDetails = query.getResultList();
            logger.debug("Se encontraron {} detalles de pedido para pedido ID: {}", orderDetails.size(), orderId);
            return orderDetails;
        } catch (Exception e) {
            logger.error("Error al buscar detalles de pedido para pedido ID: {}", orderId, e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }
}
