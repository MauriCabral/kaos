package org.example.kaos.service.implementation;

import org.example.kaos.entity.ExtraItem;
import org.example.kaos.repository.ExtraItemRepository;
import org.example.kaos.service.IExtraItemService;

import java.util.List;
import java.util.stream.Collectors;

public class ExtraItemServiceImpl implements IExtraItemService {
    private final ExtraItemRepository extraItemRepository;

    public ExtraItemServiceImpl() {
        this.extraItemRepository = new ExtraItemRepository();
    }

    @Override
    public ExtraItem getSingleExtra() {
        // extra_id = 1 para papas (extra)
        return extraItemRepository.findByExtraItemId(1);
    }

    @Override
    public List<ExtraItem> getAllCombos() {
        // extra_id = 2 para combos
        List<ExtraItem> allItems = extraItemRepository.findAll();
        return allItems.stream()
                .filter(item -> item.getExtraItemId() == 2)
                .collect(Collectors.toList());
    }

    @Override
    public boolean saveOrUpdateExtraItem(ExtraItem extraItem, boolean isNew) {
        return extraItemRepository.saveOrUpdate(extraItem, isNew);
    }

    @Override
    public boolean deleteExtraItem(Long id) {
        return extraItemRepository.delete(id);
    }

    @Override
    public boolean nameExists(Long id, String name) {
        return extraItemRepository.nameExists(id, name);
    }

    @Override
    public boolean hasExtra() {
        return getSingleExtra() != null;
    }
}