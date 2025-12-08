package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.VariantType;
import org.example.kaos.util.JpaUtil;

import java.util.List;

public class BurgerVariantRepository {

    public List<BurgerVariant> findByBurgerId(Long burgerId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<BurgerVariant> query = em.createQuery(
                    "SELECT v FROM BurgerVariant v WHERE v.burger.id = :burgerId",
                    BurgerVariant.class);
            query.setParameter("burgerId", burgerId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean saveVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            VariantType simpleType = em.createQuery(
                            "SELECT vt FROM VariantType vt WHERE vt.name = 'SIMPLE'", VariantType.class)
                    .getSingleResult();

            VariantType dobleType = em.createQuery(
                            "SELECT vt FROM VariantType vt WHERE vt.name = 'DOBLE'", VariantType.class)
                    .getSingleResult();

            VariantType tripleType = em.createQuery(
                            "SELECT vt FROM VariantType vt WHERE vt.name = 'TRIPLE'", VariantType.class)
                    .getSingleResult();

            BurgerVariant simpleVariant = BurgerVariant.builder()
                    .burger(burger)
                    .variantType(simpleType)
                    .price(simplePrice)
                    .isAvailable(true)
                    .createdByUser(burger.getCreatedByUser())
                    .build();
            em.persist(simpleVariant);

            BurgerVariant dobleVariant = BurgerVariant.builder()
                    .burger(burger)
                    .variantType(dobleType)
                    .price(doblePrice)
                    .isAvailable(true)
                    .createdByUser(burger.getCreatedByUser())
                    .build();
            em.persist(dobleVariant);

            BurgerVariant tripleVariant = BurgerVariant.builder()
                    .burger(burger)
                    .variantType(tripleType)
                    .price(triplePrice)
                    .isAvailable(true)
                    .createdByUser(burger.getCreatedByUser())
                    .build();
            em.persist(tripleVariant);

            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }

    public boolean updateVariantPrices(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            boolean updated = false;

            List<BurgerVariant> variants = em.createQuery(
                            "SELECT v FROM BurgerVariant v WHERE v.burger.id = :burgerId",
                            BurgerVariant.class)
                    .setParameter("burgerId", burger.getId())
                    .getResultList();

            for (BurgerVariant variant : variants) {
                long variantTypeId = variant.getVariantType().getId();

                if (variantTypeId == 1L) { // SIMPLE
                    variant.setPrice(simplePrice);
                } else if (variantTypeId == 2L) { // DOBLE
                    variant.setPrice(doblePrice);
                } else if (variantTypeId == 3L) { // TRIPLE
                    variant.setPrice(triplePrice);
                }
                em.merge(variant);
                updated = true;
            }

            em.getTransaction().commit();
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public BurgerVariant findByBurgerVariantId(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        BurgerVariant burgerVariant = null;
        try {
            TypedQuery<BurgerVariant> query = em.createQuery(
                    "SELECT v FROM BurgerVariant v WHERE v.id = :id",
                    BurgerVariant.class);
            query.setParameter("id", id);
            burgerVariant =query.getSingleResult();
        } catch (NoResultException e) {
            // burgerVariant no encontrado
        } finally {
            em.close();
        }
        return burgerVariant;
    }
}
