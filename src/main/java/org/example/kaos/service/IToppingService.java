package org.example.kaos.service;

import org.example.kaos.entity.Topping;

import java.util.List;

public interface IToppingService {
    List<Topping> getAllToppings();
    Topping createTopping(Topping topping);

    boolean deleteTopping(Long id);
}
