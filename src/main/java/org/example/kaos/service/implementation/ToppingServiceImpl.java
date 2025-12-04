package org.example.kaos.service.implementation;

import org.example.kaos.entity.Topping;
import org.example.kaos.repository.BurgerRepository;
import org.example.kaos.repository.BurgerVariantRepository;
import org.example.kaos.repository.ToppingRepository;
import org.example.kaos.service.IToppingService;

import java.util.List;

public class ToppingServiceImpl implements IToppingService {
    private final ToppingRepository toppingRepository;

    public ToppingServiceImpl() {
        this.toppingRepository = new ToppingRepository();
    }

    @Override
    public List<Topping> getAllToppings() {
        return toppingRepository.findAll();
    }

    @Override
    public Topping createTopping(Topping topping) {
        return toppingRepository.save(topping);
    }

    @Override
    public boolean deleteTopping(Long id) {
        return toppingRepository.delete(id);
    }
}
