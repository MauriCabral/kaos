package org.example.kaos.service.implementation;

import org.example.kaos.entity.Burger;
import org.example.kaos.repository.BurgerRepository;
import org.example.kaos.service.IBurgerService;

import java.util.List;

public class BurgerServiceImpl implements IBurgerService {

    private final BurgerRepository burgerRepository;

    public BurgerServiceImpl() {
        this.burgerRepository = new BurgerRepository();
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
    public boolean codeExists(String code) {
        return burgerRepository.existsByCode(code);
    }

    @Override
    public boolean nameExists(String name) {
        return burgerRepository.existsByName(name);
    }

    @Override
    public void saveBurgerWithVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice) {
        Burger savedBurger = burgerRepository.save(burger);
        burgerRepository.saveBurgerWithVariants(savedBurger, simplePrice, doblePrice, triplePrice);
    }
}