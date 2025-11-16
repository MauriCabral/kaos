package org.example.kaos.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.entity.VariantType;
import org.example.kaos.util.JpaUtil;

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

    public boolean existsByCode(String code) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(b) FROM Burger b WHERE b.code = :code", Long.class);
            query.setParameter("code", code);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public boolean existsByName(String name) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(b) FROM Burger b WHERE b.name = :name", Long.class);
            query.setParameter("name", name);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    public void saveBurgerWithVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            Burger managedBurger = em.merge(burger);

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
                    .burger(managedBurger)
                    .variantType(simpleType)
                    .price(simplePrice)
                    .isAvailable(true)
                    .createdByUser(managedBurger.getCreatedByUser())
                    .build();
            em.persist(simpleVariant);

            BurgerVariant dobleVariant = BurgerVariant.builder()
                    .burger(managedBurger)
                    .variantType(dobleType)
                    .price(doblePrice)
                    .isAvailable(true)
                    .createdByUser(managedBurger.getCreatedByUser())
                    .build();
            em.persist(dobleVariant);

            BurgerVariant tripleVariant = BurgerVariant.builder()
                    .burger(managedBurger)
                    .variantType(tripleType)
                    .price(triplePrice)
                    .isAvailable(true)
                    .createdByUser(managedBurger.getCreatedByUser())
                    .build();
            em.persist(tripleVariant);

            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}