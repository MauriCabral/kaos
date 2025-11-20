package org.example.kaos.service;

import org.example.kaos.entity.Burger;
import org.example.kaos.entity.BurgerVariant;

import java.util.List;

public interface IBurgerService {
    List<Burger> getAllBurgers();
    Burger saveBurger(Burger burger);
    boolean codeExists(long id, String code);
    boolean nameExists(long id, String name);
    boolean saveBurgerWithVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice);

    List<BurgerVariant> getVariantsByBurgerId(int id);
    boolean updateBurgerWithVariants(Burger burger, double simplePrice, double doblePrice, double triplePrice);
    Burger getBurgerById(int burgerId);

    boolean deleteBurgerById(long id);
}
