package org.example.kaos.service.implementation;

import org.example.kaos.entity.Combo;
import org.example.kaos.repository.ComboRepository;
import org.example.kaos.service.IComboService;

import java.util.List;

public class ComboServiceImpl implements IComboService {
    private final ComboRepository comboRepository;

    public ComboServiceImpl() {
        this.comboRepository = new ComboRepository();
    }

    @Override
    public Combo getSingleCombo() {
        List<Combo> combos = comboRepository.findAll();
        return combos.isEmpty() ? null : combos.get(0);
    }

    @Override
    public void saveCombo(Combo combo) {
        comboRepository.save(combo);
    }

    @Override
    public List<Combo> getAllCombos() {
        return comboRepository.findAll();
    }
}
