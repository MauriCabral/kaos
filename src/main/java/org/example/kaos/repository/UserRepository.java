package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.User;
import org.example.kaos.util.JpaUtil;

public class UserRepository {

    public User save(User user) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (user.getId() == null) {
                em.persist(user);
            } else {
                user = em.merge(user);
            }

            em.getTransaction().commit();
            return user;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving user", e);
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        User user = null;
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            user = query.getSingleResult();
        } catch (NoResultException e) {
            // usuario no encontrado
        } finally {
            em.close();
        }
        return user;
    }

    public User findUserById(Long userId) {
        EntityManager em = JpaUtil.getEntityManager();
        User user = null;
        try {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.id = :userId", User.class);
            query.setParameter("userId", userId);
            user = query.getSingleResult();
        } catch (NoResultException e) {
        } finally {
            em.close();
        }
        return user;
    }
}
