package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.User;
import org.example.kaos.entity.VariantType;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BurgerRepository {
    private static final Logger logger = LoggerFactory.getLogger(BurgerRepository.class);

    public List<Burger> findAll() {
        logger.debug("Buscando todas las hamburguesas");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Burger> query = em.createQuery(
                    "SELECT b FROM Burger b ORDER BY b.name", Burger.class);
            List<Burger> burgers = query.getResultList();
            logger.debug("Se encontraron {} hamburguesas", burgers.size());
            return burgers;
        } catch (Exception e) {
            logger.error("Error al buscar todas las hamburguesas", e);
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Burger save(Burger burger) {
        logger.debug("Guardando hamburguesa: {}", burger.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (burger.getId() == 0) {
                em.persist(burger);
                logger.info("Hamburguesa creada: {}", burger.getName());
            } else {
                burger = em.merge(burger);
                logger.info("Hamburguesa actualizada: {}", burger.getName());
            }
            em.getTransaction().commit();
            return burger;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al guardar hamburguesa: {}", burger.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean existsByCode(long id, String code) {
        logger.debug("Verificando existencia de hamburguesa por código: {}, id: {}", code, id);
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
            boolean exists = query.getSingleResult() > 0;
            logger.debug("Hamburguesa con código {} {}", code, exists ? "existe" : "no existe");
            return exists;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de hamburguesa por código: {}, id: {}", code, id, e);
            return false;
        } finally {
            em.close();
        }
    }

    public boolean existsByName(long id, String name) {
        logger.debug("Verificando existencia de hamburguesa por nombre: {}, id: {}", name, id);
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
            boolean exists = query.getSingleResult() > 0;
            logger.debug("Hamburguesa con nombre {} {}", name, exists ? "existe" : "no existe");
            return exists;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de hamburguesa por nombre: {}, id: {}", name, id, e);
            return false;
        } finally {
            em.close();
        }
    }

    public Burger saveBurger(Burger burger) {
        logger.debug("Guardando hamburguesa: {}", burger.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Burger savedBurger;
            if (burger.getId() == null) {
                em.persist(burger);
                em.flush(); // Ensure id is assigned
                savedBurger = burger;
                logger.info("Hamburguesa creada: {}", burger.getName());
            } else {
                savedBurger = em.merge(burger);
                logger.info("Hamburguesa actualizada: {}", burger.getName());
            }
            em.getTransaction().commit();
            return savedBurger;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al guardar hamburguesa: {}", burger.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }


    public Burger updateBurger(Burger burger) {
        logger.debug("Actualizando hamburguesa: {}", burger.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Burger updatedBurger = em.merge(burger);
            em.getTransaction().commit();
            logger.info("Hamburguesa actualizada: {}", burger.getName());
            return updatedBurger;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al actualizar hamburguesa: {}", burger.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }


    public Burger findBurgerById(int burgerId) {
        logger.debug("Buscando hamburguesa por ID: {}", burgerId);
        EntityManager em = JpaUtil.getEntityManager();
        Burger burger = null;
        try {
            TypedQuery<Burger> query = em.createQuery(
                    "SELECT b FROM Burger b WHERE b.id = :burgerId", Burger.class);
            query.setParameter("burgerId", burgerId);
            burger = query.getSingleResult();
            logger.debug("Hamburguesa encontrada: {}", burger.getName());
        } catch (NoResultException e) {
            logger.warn("Hamburguesa no encontrada por ID: {}", burgerId);
        } catch (Exception e) {
            logger.error("Error al buscar hamburguesa por ID: {}", burgerId, e);
        } finally {
            em.close();
        }
        return burger;
    }

    public boolean deleteBurgerById(long burgerId) {
        logger.debug("Eliminando hamburguesa por ID: {}", burgerId);
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
                logger.info("Hamburguesa eliminada: {}", burger.getName());
                return true;
            }

            em.getTransaction().commit();
            logger.warn("No se encontró hamburguesa por ID para eliminar: {}", burgerId);
            return false;

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al eliminar hamburguesa: {}", e.getMessage(), e);
            }
            logger.error("Error al eliminar hamburguesa por ID: {}", burgerId, e);
            throw e;
        } finally {
            em.close();
        }
    }
}