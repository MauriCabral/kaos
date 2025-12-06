package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.OrderDetailTopping;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class OrderDetailToppingRepository {
    public OrderDetailTopping save(OrderDetailTopping topping) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (topping.getId() == null) {
                em.persist(topping);
            } else {
                topping = em.merge(topping);
            }

            em.getTransaction().commit();
            return topping;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetailTopping> saveAll(List<OrderDetailTopping> toppings) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            for (OrderDetailTopping topping : toppings) {
                if (topping.getId() == null) {
                    em.persist(topping);
                } else {
                    em.merge(topping);
                }
            }

            em.getTransaction().commit();
            return toppings;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetailTopping> findByOrderDetailId(Long orderDetailId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<OrderDetailTopping> query = em.createQuery(
                    "SELECT odt FROM OrderDetailTopping odt WHERE odt.orderDetail.id = :orderDetailId",
                    OrderDetailTopping.class);
            query.setParameter("orderDetailId", orderDetailId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
