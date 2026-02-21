package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.ExtraItem;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExtraItemRepository {
    private static final Logger logger = LoggerFactory.getLogger(ExtraItemRepository.class);

    public List<ExtraItem> findAll() {
        logger.debug("Buscando todos los items extra");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<ExtraItem> query = em.createQuery(
                    "SELECT e FROM ExtraItem e ORDER BY e.id", ExtraItem.class);
            List<ExtraItem> extraItems = query.getResultList();
            logger.debug("Se encontraron {} items extra", extraItems.size());
            return extraItems;
        } catch (Exception e) {
            logger.error("Error al buscar todos los items extra", e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public ExtraItem findById(Long id) {
        logger.debug("Buscando item extra por ID: {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            ExtraItem extraItem = em.find(ExtraItem.class, id);
            if (extraItem != null) {
                logger.debug("Item extra encontrado: {}", extraItem.getName());
            } else {
                logger.warn("Item extra no encontrado por ID: {}", id);
            }
            return extraItem;
        } catch (Exception e) {
            logger.error("Error al buscar item extra por ID: {}", id, e);
            return null;
        } finally {
            em.close();
        }
    }

    public ExtraItem findByExtraItemId(int extraItemId) {
        logger.debug("Buscando item extra por extraItemId: {}", extraItemId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<ExtraItem> query = em.createQuery(
                    "SELECT e FROM ExtraItem e WHERE e.extraItemId = :extraItemId", ExtraItem.class);
            query.setParameter("extraItemId", extraItemId);
            List<ExtraItem> results = query.getResultList();
            if (results.isEmpty()) {
                logger.warn("Item extra no encontrado por extraItemId: {}", extraItemId);
                return null;
            }
            logger.debug("Item extra encontrado: {}", results.get(0).getName());
            return results.get(0);
        } catch (Exception e) {
            logger.error("Error al buscar item extra por extraItemId: {}", extraItemId, e);
            return null;
        } finally {
            em.close();
        }
    }

    public boolean saveOrUpdate(ExtraItem extraItem, boolean isNew) {
        logger.debug("{} item extra: {}", isNew ? "Guardando" : "Actualizando", extraItem.getName());
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            if (isNew) {
                extraItem.setId(null);
                em.persist(extraItem);
                logger.info("Item extra guardado: {}", extraItem.getName());
            } else {
                em.merge(extraItem);
                logger.info("Item extra actualizado: {}", extraItem.getName());
            }

            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al {} item extra: {}", isNew ? "guardar" : "actualizar", extraItem.getName(), e);
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
        logger.debug("Eliminando item extra por ID: {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            ExtraItem extraItem = em.find(ExtraItem.class, id);
            if (extraItem != null) {
                em.remove(extraItem);
                em.getTransaction().commit();
                logger.info("Item extra eliminado: {}", extraItem.getName());
                return true;
            } else {
                em.getTransaction().rollback();
                logger.warn("No se encontró item extra por ID: {}", id);
                return false;
            }
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al eliminar item extra por ID: {}", id, e);
            return false;
        } finally {
            em.close();
        }
    }

    public boolean nameExists(Long id, String name) {
        logger.debug("Verificando existencia de item extra con nombre: {}, id: {}", name, id);
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
            boolean exists = query.getSingleResult() > 0;
            logger.debug("Item extra con nombre {} {}", name, exists ? "existe" : "no existe");
            return exists;
        } catch (Exception e) {
            logger.error("Error al verificar existencia de item extra con nombre: {}, id: {}", name, id, e);
            return false;
        } finally {
            em.close();
        }
    }
}