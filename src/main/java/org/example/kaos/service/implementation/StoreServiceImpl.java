package org.example.kaos.service.implementation;

import org.example.kaos.entity.Store;
import org.example.kaos.repository.StoreRepository;
import org.example.kaos.service.IStoreService;

import java.util.List;

public class StoreServiceImpl implements IStoreService {
    private final StoreRepository storeRepository = new StoreRepository();

    @Override
    public List<Store> getAllStore() {
        List<Store> store = storeRepository.findAll();
        if (store != null) {
            return store;
        }
        return null;
    }
}
