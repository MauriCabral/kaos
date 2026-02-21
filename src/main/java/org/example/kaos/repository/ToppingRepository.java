package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.Topping;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ToppingRepository {
    private static final Logger logger = LoggerFactory.getLogger(ToppingRepository.class);

    public List<Topping> findAll() {
        logger.debug("Buscando todos los toppings");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Topping> query = em.createQuery(
                    "SELECT b FROM Topping b ORDER BY b.name", Topping.class);
            List<Topping> toppings = query.getResultList();
            logger.debug("Se encontraron {} toppings", toppings.size());
            return toppings;
        } catch (Exception e) {
            logger.error("Error al buscar todos los toppings", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Topping save(Topping topping) {
        logger.debug("Guardando topping: {}", topping.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (topping.getId() == null) {
                em.persist(topping);
                logger.info("Topping creado: {}", topping.getName());
            } else {
                topping = em.merge(topping);
                logger.info("Topping actualizado: {}", topping.getName());
            }
            em.getTransaction().commit();
            return topping;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al guardar topping: {}", topping.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean delete(Long id) {
        logger.debug("Eliminando topping por ID: {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Topping topping = em.find(Topping.class, id);
            if (topping != null) {
                em.remove(topping);
                em.getTransaction().commit();
                logger.info("Topping eliminado: {}", topping.getName());
                return true;
            }

            em.getTransaction().commit();
            logger.warn("No se encontró topping por ID para eliminar: {}", id);
            return false;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al eliminar topping: {}", e.getMessage(), e);
            }
            logger.error("Error al eliminar topping por ID: {}", id, e);
            throw e;
        } finally {
            em.close();
        }
    }
}
