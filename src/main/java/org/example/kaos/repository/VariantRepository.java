package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.VariantType;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class VariantRepository {
    public List<VariantType> findAll() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<VariantType> query = em.createQuery(
                    "SELECT bv FROM VariantType bv ORDER BY bv.id", VariantType.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
