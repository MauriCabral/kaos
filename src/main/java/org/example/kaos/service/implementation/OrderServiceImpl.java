package org.example.kaos.service.implementation;

import org.example.kaos.entity.Order;
import org.example.kaos.repository.OrderDetailRepository;
import org.example.kaos.repository.OrderRepository;
import org.example.kaos.repository.StoreRepository;
import org.example.kaos.repository.UserRepository;
import org.example.kaos.service.IOrderService;

import java.util.List;

public class OrderServiceImpl implements IOrderService {
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final OrderDetailServiceImpl orderDetailService;

    public OrderServiceImpl() {
        this.orderRepository = new OrderRepository();
        this.storeRepository = new StoreRepository();
        this.userRepository = new UserRepository();
        this.orderDetailService = new OrderDetailServiceImpl();
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
    public List<Order> getAllOrders(Boolean isAdmin) {
        return orderRepository.findAll(isAdmin);
    }

    @Override
    public Order updateOrder(Order order) {
        return orderRepository.update(order);
    }
}
