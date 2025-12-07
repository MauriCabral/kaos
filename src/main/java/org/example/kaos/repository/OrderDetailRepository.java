package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.OrderDetail;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class OrderDetailRepository {

    public OrderDetail save(OrderDetail orderDetail) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (orderDetail.getId() == null) {
                em.persist(orderDetail);
            } else {
                orderDetail = em.merge(orderDetail);
            }

            em.getTransaction().commit();
            return orderDetail;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetail> saveAll(List<OrderDetail> orderDetails) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            for (OrderDetail detail : orderDetails) {
                if (detail.getId() == null) {
                    em.persist(detail);
                } else {
                    em.merge(detail);
                }
            }

            em.getTransaction().commit();
            return orderDetails;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetail> findByOrderId(Long orderId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<OrderDetail> query = em.createQuery(
                    "SELECT od FROM OrderDetail od WHERE od.order.id = :orderId", OrderDetail.class);
            query.setParameter("orderId", orderId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
