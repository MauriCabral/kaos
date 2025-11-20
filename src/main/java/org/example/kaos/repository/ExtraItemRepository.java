package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.ExtraItem;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class ExtraItemRepository {

    public List<ExtraItem> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<ExtraItem> query = em.createQuery(
                    "SELECT e FROM ExtraItem e ORDER BY e.id", ExtraItem.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public ExtraItem findById(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.find(ExtraItem.class, id);
        } finally {
            em.close();
        }
    }

    public ExtraItem findByExtraItemId(int extraItemId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<ExtraItem> query = em.createQuery(
                    "SELECT e FROM ExtraItem e WHERE e.extraItemId = :extraItemId", ExtraItem.class);
            query.setParameter("extraItemId", extraItemId);
            List<ExtraItem> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    public boolean saveOrUpdate(ExtraItem extraItem, boolean isNew) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (isNew) {
                extraItem.setId(null);
                em.persist(extraItem);
            } else {
                em.merge(extraItem);
            }

            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean save(ExtraItem extraItem) {
        return saveOrUpdate(extraItem, true);
    }

    public boolean update(ExtraItem extraItem) {
        return saveOrUpdate(extraItem, false);
    }

    public boolean delete(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ExtraItem extraItem = em.find(ExtraItem.class, id);
            if (extraItem != null) {
                em.remove(extraItem);
                em.getTransaction().commit();
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean nameExists(Long id, String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            String queryString;
            if (id == null || id == 0) {
                queryString = "SELECT COUNT(e) FROM ExtraItem e WHERE e.name = :name";
            } else {
                queryString = "SELECT COUNT(e) FROM ExtraItem e WHERE e.name = :name AND e.id != :id";
            }

            TypedQuery<Long> query = em.createQuery(queryString, Long.class);
            query.setParameter("name", name);
            if (id != null && id != 0) {
                query.setParameter("id", id);
            }
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }
}