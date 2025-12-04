package org.example.kaos.service.implementation;

import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;
import org.example.kaos.repository.BurgerRepository;
import org.example.kaos.repository.BurgerVariantRepository;
import org.example.kaos.service.IBurgerService;

import java.util.List;

public class BurgerServiceImpl implements IBurgerService {

    private final BurgerVariantRepository burgerVariantRepository;
    private final BurgerRepository burgerRepository;

    public BurgerServiceImpl() {
        this.burgerRepository = new BurgerRepository();
        this.burgerVariantRepository = new BurgerVariantRepository();
    }

    @Override
    public List<Burger> getAllBurgers() {
        return burgerRepository.findAll();
    }

    @Override
    public Burger saveBurger(Burger burger) {
        return burgerRepository.save(burger);
    }

    @Override
    public boolean codeExists(long id, String code) {
        return burgerRepository.existsByCode(id, code);
    }

    @Override
    public boolean nameExists(long id, String name) {
        return burgerRepository.existsByName(id, name);
    }

    @Override
    public boolean saveBurgerWithVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        try {
            Burger savedBurger = burgerRepository.saveBurger(burger);

            if (savedBurger != null && savedBurger.getId() > 0) {
                return burgerVariantRepository.saveVariants(savedBurger, simplePrice, doblePrice, triplePrice);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<BurgerVariant> getVariantsByBurgerId(long burgerId) {
        return burgerVariantRepository.findByBurgerId(burgerId);
    }

    @Override
    public boolean updateBurgerWithVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        return burgerRepository.updateBurgerWithVariants(burger, simplePrice, doblePrice, triplePrice);
    }

    @Override
    public Burger getBurgerById(int burgerId) {
        return burgerRepository.findBurgerById(burgerId);
    }

    @Override
    public boolean deleteBurgerById(long burgerId) {
        return burgerRepository.deleteBurgerById(burgerId);
    }
}