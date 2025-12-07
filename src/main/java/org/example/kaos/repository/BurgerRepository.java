package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.User;
import org.example.kaos.entity.VariantType;
import org.example.kaos.util.JpaUtil;

import java.util.ArrayList;
import java.util.List;

public class BurgerRepository {

    public List<Burger> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Burger> query = em.createQuery(
                    "SELECT b FROM Burger b ORDER BY b.name", Burger.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Burger save(Burger burger) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (burger.getId() == 0) {
                em.persist(burger);
            } else {
                burger = em.merge(burger);
            }
            em.getTransaction().commit();
            return burger;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean existsByCode(long id, String code) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String queryString;
            if (id == 0) {
                queryString = "SELECT COUNT(b) FROM Burger b WHERE b.code = :code";
            } else {
                queryString = "SELECT COUNT(b) FROM Burger b WHERE b.code = :code AND b.id != :id";
            }

            TypedQuery<Long> query = em.createQuery(queryString, Long.class);
            query.setParameter("code", code);
            if (id != 0) {
                query.setParameter("id", id);
            }
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByName(long id, String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String queryString;
            if (id == 0) {
                queryString = "SELECT COUNT(b) FROM Burger b WHERE b.name = :name";
            } else {
                queryString = "SELECT COUNT(b) FROM Burger b WHERE b.name = :name AND b.id != :id";
            }

            TypedQuery<Long> query = em.createQuery(queryString, Long.class);
            query.setParameter("name", name);
            if (id != 0) {
                query.setParameter("id", id);
            }
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public Burger saveBurger(Burger burger) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Burger savedBurger;
            if (burger.getId() == 0) {
                em.persist(burger);
                savedBurger = burger;
            } else {
                savedBurger = em.merge(burger);
            }
            em.getTransaction().commit();
            return savedBurger;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Burger updateBurger(Burger burger) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Burger updatedBurger = em.merge(burger);
            em.getTransaction().commit();
            return updatedBurger;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Burger findBurgerById(int burgerId) {
        EntityManager em = JpaUtil.getEntityManager();
        Burger burger = null;
        try {
            TypedQuery<Burger> query = em.createQuery(
                    "SELECT b FROM Burger b WHERE b.id = :burgerId", Burger.class);
            query.setParameter("burgerId", burgerId);
            burger = query.getSingleResult();
        } catch (NoResultException e) {
            // burger no encontrado
        } finally {
            em.close();
        }
        return burger;
    }

    public boolean deleteBurgerById(long burgerId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Burger burger = em.find(Burger.class, burgerId);
            if (burger != null) {
                for (BurgerVariant variant : new ArrayList<>(burger.getVariants())) {
                    em.remove(variant);
                }
                burger.getVariants().clear();

                em.remove(burger);
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