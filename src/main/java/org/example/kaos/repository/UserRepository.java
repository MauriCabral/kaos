package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.User;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public User save(User user) {
        logger.debug("Guardando usuario: {}", user.getUsername());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (user.getId() == null) {
                em.persist(user);
                logger.info("Usuario creado: {}", user.getUsername());
            } else {
                user = em.merge(user);
                logger.info("Usuario actualizado: {}", user.getUsername());
            }

            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
                logger.error("Rollback de transacción al guardar usuario: {}", e.getMessage(), e);
            }
            logger.error("Error al guardar usuario: {}", user.getUsername(), e);
            throw new RuntimeException("Error saving user", e);
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        logger.debug("Buscando usuario por nombre de usuario: {}", username);
        EntityManager em = JpaUtil.getEntityManager();
        User user = null;
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            user = query.getSingleResult();
            logger.debug("Usuario encontrado: {}", username);
        } catch (NoResultException e) {
            logger.warn("Usuario no encontrado: {}", username);
        } catch (Exception e) {
            logger.error("Error al buscar usuario por nombre de usuario: {}", username, e);
        } finally {
            em.close();
        }
        return user;
    }

    public User findUserById(Long userId) {
        logger.debug("Buscando usuario por ID: {}", userId);
        EntityManager em = JpaUtil.getEntityManager();
        User user = null;
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.id = :userId", User.class);
            query.setParameter("userId", userId);
            user = query.getSingleResult();
            logger.debug("Usuario encontrado por ID: {}", userId);
        } catch (NoResultException e) {
            logger.warn("Usuario no encontrado por ID: {}", userId);
        } catch (Exception e) {
            logger.error("Error al buscar usuario por ID: {}", userId, e);
        } finally {
            em.close();
        }
        return user;
    }
}
