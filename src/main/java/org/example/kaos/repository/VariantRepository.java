package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.VariantType;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VariantRepository {
    private static final Logger logger = LoggerFactory.getLogger(VariantRepository.class);

    public List<VariantType> findAll() {
        logger.debug("Buscando todos los tipos de variantes");
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<VariantType> query = em.createQuery(
                    "SELECT bv FROM VariantType bv ORDER BY bv.id", VariantType.class);
            List<VariantType> variantTypes = query.getResultList();
            logger.debug("Se encontraron {} tipos de variantes", variantTypes.size());
            return variantTypes;
        } catch (Exception e) {
            logger.error("Error al buscar todos los tipos de variantes", e);
            return List.of();
        } finally {
            em.close();
        }
    }
}
