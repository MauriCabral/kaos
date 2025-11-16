package org.example.kaos.service;

import org.example.kaos.entity.Burger;

import java.util.List;

public interface IBurgerService {
    List<Burger> getAllBurgers();
    Burger saveBurger(Burger burger);
    boolean codeExists(String code);
    boolean nameExists(String name);
    void saveBurgerWithVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice);
}
