package org.example.kaos.service.implementation;

import org.example.kaos.entity.Delivery;
import org.example.kaos.repository.DeliveryRepository;
import org.example.kaos.service.IDeliveryService;

import java.util.List;

public class DeliveryServiceImpl implements IDeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryServiceImpl() {
        this.deliveryRepository = new DeliveryRepository();
    }

    @Override
    public List<Delivery> findAll() {
        return deliveryRepository.findAll();
    }

    @Override
    public Delivery findById(Long id) {
        return deliveryRepository.findById(id);
    }

    @Override
    public Delivery save(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }

    @Override
    public Delivery update(Delivery delivery) {
        return deliveryRepository.update(delivery);
    }

    @Override
    public void delete(Long id) {
        deliveryRepository.delete(id);
    }

    @Override
    public List<Delivery> findByName(String name) {
        return deliveryRepository.findByName(name);
    }

    @Override
    public List<Delivery> findByStoreId(Long id) {
        return deliveryRepository.findAllByStoreId(id);
    }
}
