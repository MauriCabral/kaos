package org.example.kaos.service;

import org.example.kaos.entity.Delivery;

import java.util.List;

public interface IDeliveryService {
    List<Delivery> findAll();
    Delivery findById(Long id);
    Delivery save(Delivery delivery);
    Delivery update(Delivery delivery);
    void delete(Long id);
    List<Delivery> findByName(String name);
    List<Delivery> findByStoreId(Long id);
}