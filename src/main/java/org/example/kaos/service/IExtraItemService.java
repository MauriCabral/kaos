package org.example.kaos.service;

import org.example.kaos.entity.ExtraItem;

import java.util.List;

public interface IExtraItemService {
    ExtraItem getSingleExtra();
    boolean hasExtra();
    List<ExtraItem> getAllCombos();
    boolean saveOrUpdateExtraItem(ExtraItem extraItem, boolean isNew); // Asegurar que este nombre coincida
    boolean deleteExtraItem(Long id);
    boolean nameExists(Long id, String name);
}