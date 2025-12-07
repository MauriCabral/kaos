package org.example.kaos.service.implementation;

import org.example.kaos.entity.Order;
import org.example.kaos.repository.OrderRepository;
import org.example.kaos.repository.StoreRepository;
import org.example.kaos.repository.UserRepository;
import org.example.kaos.service.IOrderService;

import java.util.List;

public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl() {
        this.orderRepository = new OrderRepository();
        this.storeRepository = new StoreRepository();
        this.userRepository = new UserRepository();
    }

    @Override
    public Order createOrder(Order order) {
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(orderRepository.generateUniqueOrderNumber());
        }
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getAllOrders() {
        List<Order> orderList = orderRepository.findAll();
        for (Order ord : orderList) {
            ord.setStore(storeRepository.findStoreById(ord.getStore().getId()));
            ord.setCreatedByUser(userRepository.findUserById(ord.getCreatedByUser().getId()));
        }
        return orderList;
    }
}
