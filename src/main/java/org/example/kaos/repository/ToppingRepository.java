package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.Topping;
import org.example.kaos.util.JpaUtil;

import java.util.ArrayList;
import java.util.List;

public class ToppingRepository {

    public List<Topping> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Topping> query = em.createQuery(
                    "SELECT b FROM Topping b ORDER BY b.name", Topping.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Topping save(Topping topping) {
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
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Topping topping = em.find(Topping.class, id);
            if (topping != null) {
                em.remove(topping);
                em.getTransaction().commit();
                return true;
            }

            em.getTransaction().commit();
            return false;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
