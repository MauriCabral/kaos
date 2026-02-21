package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.VariantType;
import org.example.kaos.util.JpaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BurgerVariantRepository {
    private static final Logger logger = LoggerFactory.getLogger(BurgerVariantRepository.class);

    public List<BurgerVariant> findByBurgerId(Long burgerId) {
        logger.debug("Buscando variantes de hamburguesa por ID de hamburguesa: {}", burgerId);
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<BurgerVariant> query = em.createQuery(
                    "SELECT v FROM BurgerVariant v WHERE v.burger.id = :burgerId",
                    BurgerVariant.class);
            query.setParameter("burgerId", burgerId);
            List<BurgerVariant> variants = query.getResultList();
            logger.debug("Se encontraron {} variantes para hamburguesa ID: {}", variants.size(), burgerId);
            return variants;
        } catch (Exception e) {
            logger.error("Error al buscar variantes de hamburguesa por ID de hamburguesa: {}", burgerId, e);
            return List.of();
        } finally {
            em.close();
        }
    }

    public boolean saveVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        logger.debug("Guardando variantes para hamburguesa: {}", burger.getName());
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
            logger.debug("Variante SIMPLE guardada: {}", simplePrice);

            BurgerVariant dobleVariant = BurgerVariant.builder()
                    .burger(burger)
                    .variantType(dobleType)
                    .price(doblePrice)
                    .isAvailable(true)
                    .createdByUser(burger.getCreatedByUser())
                    .build();
            em.persist(dobleVariant);
            logger.debug("Variante DOBLE guardada: {}", doblePrice);

            BurgerVariant tripleVariant = BurgerVariant.builder()
                    .burger(burger)
                    .variantType(tripleType)
                    .price(triplePrice)
                    .isAvailable(true)
                    .createdByUser(burger.getCreatedByUser())
                    .build();
            em.persist(tripleVariant);
            logger.debug("Variante TRIPLE guardada: {}", triplePrice);

            em.getTransaction().commit();
            logger.info("Variantes guardadas para hamburguesa: {}", burger.getName());
            return true;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al guardar variantes para hamburguesa: {}", burger.getName(), e);
            return false;
        } finally {
            em.close();
        }
    }

    public boolean updateVariantPrices(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        logger.debug("Actualizando precios de variantes para hamburguesa: {}", burger.getName());
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
                double oldPrice = variant.getPrice();

                if (variantTypeId == 1L) { // SIMPLE
                    variant.setPrice(simplePrice);
                    logger.debug("Precio variante SIMPLE actualizado de {} a {}", oldPrice, simplePrice);
                } else if (variantTypeId == 2L) { // DOBLE
                    variant.setPrice(doblePrice);
                    logger.debug("Precio variante DOBLE actualizado de {} a {}", oldPrice, doblePrice);
                } else if (variantTypeId == 3L) { // TRIPLE
                    variant.setPrice(triplePrice);
                    logger.debug("Precio variante TRIPLE actualizado de {} a {}", oldPrice, triplePrice);
                }
                em.merge(variant);
                updated = true;
            }

            em.getTransaction().commit();
            logger.info("Precios de variantes actualizados para hamburguesa: {}", burger.getName());
            return updated;
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.error("Error al actualizar precios de variantes para hamburguesa: {}", burger.getName(), e);
            throw e;
        } finally {
            em.close();
        }
    }

    public BurgerVariant findByBurgerVariantId(Long id) {
        logger.debug("Buscando variante de hamburguesa por ID: {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        BurgerVariant burgerVariant = null;
        try {
            TypedQuery<BurgerVariant> query = em.createQuery(
                    "SELECT v FROM BurgerVariant v WHERE v.id = :id",
                    BurgerVariant.class);
            query.setParameter("id", id);
            burgerVariant =query.getSingleResult();
            logger.debug("Variante de hamburguesa encontrada: {}", burgerVariant.getVariantType().getName());
        } catch (NoResultException e) {
            logger.warn("Variante de hamburguesa no encontrada por ID: {}", id);
        } catch (Exception e) {
            logger.error("Error al buscar variante de hamburguesa por ID: {}", id, e);
        } finally {
            em.close();
        }
        return burgerVariant;
    }
}
