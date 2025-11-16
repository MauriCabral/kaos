package org.example.kaos.service;

import org.example.kaos.entity.Combo;

import java.util.List;

public interface IComboService {
    Combo getSingleCombo();
    void saveCombo(Combo combo);
    List<Combo> getAllCombos();
}
