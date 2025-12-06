package org.example.kaos.service.implementation;

import org.example.kaos.entity.Order;
import org.example.kaos.repository.OrderRepository;
import org.example.kaos.service.IOrderService;

public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;

    public OrderServiceImpl() {
        this.orderRepository = new OrderRepository();
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
}
