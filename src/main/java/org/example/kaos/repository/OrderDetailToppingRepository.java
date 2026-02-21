package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.OrderDetailTopping;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OrderDetailToppingRepository {
    private static final Logger logger = LoggerFactory.getLogger(OrderDetailToppingRepository.class);

    public OrderDetailTopping save(OrderDetailTopping topping) {
        logger.debug("Guardando topping de detalle de pedido: {}", topping.getTopping().getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (topping.getId() == null) {
                em.persist(topping);
                logger.info("Topping de detalle de pedido guardado: {}", topping.getTopping().getName());
            } else {
                topping = em.merge(topping);
                logger.info("Topping de detalle de pedido actualizado: {}", topping.getTopping().getName());
            }

            em.getTransaction().commit();
            return topping;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al guardar topping de detalle de pedido: {}", e.getMessage(), e);
            }
            logger.error("Error al guardar topping de detalle de pedido: {}", topping.getTopping().getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetailTopping> saveAll(List<OrderDetailTopping> toppings) {
        logger.debug("Guardando {} toppings de detalle de pedido", toppings.size());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            for (OrderDetailTopping topping : toppings) {
                if (topping.getId() == null) {
                    em.persist(topping);
                    logger.debug("Topping de detalle de pedido persistido: {}", topping.getTopping().getName());
                } else {
                    em.merge(topping);
                    logger.debug("Topping de detalle de pedido mergeado: {}", topping.getTopping().getName());
                }
            }

            em.getTransaction().commit();
            logger.info("{} toppings de detalle de pedido guardados exitosamente", toppings.size());
            return toppings;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al guardar toppings de detalle de pedido: {}", e.getMessage(), e);
            }
            logger.error("Error al guardar toppings de detalle de pedido: {}", e.getMessage(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public List<OrderDetailTopping> findByOrderDetailId(Long orderDetailId) {
        logger.debug("Buscando toppings de detalle de pedido por ID de detalle: {}", orderDetailId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<OrderDetailTopping> query = em.createQuery(
                    "SELECT odt FROM OrderDetailTopping odt WHERE odt.orderDetail.id = :orderDetailId",
                    OrderDetailTopping.class);
            query.setParameter("orderDetailId", orderDetailId);
            List<OrderDetailTopping> orderDetailToppings = query.getResultList();
            logger.debug("Se encontraron {} toppings para detalle de pedido ID: {}", orderDetailToppings.size(), orderDetailId);
            return orderDetailToppings;
        } catch (Exception e) {
            logger.error("Error al buscar toppings de detalle de pedido por ID de detalle: {}", orderDetailId, e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
