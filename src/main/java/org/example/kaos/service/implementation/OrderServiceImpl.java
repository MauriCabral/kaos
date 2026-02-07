package org.example.kaos.service.implementation;

import org.example.kaos.entity.Order;
import org.example.kaos.repository.OrderRepository;
import org.example.kaos.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class OrderServiceImpl implements IOrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;

    public OrderServiceImpl() {
        this.orderRepository = new OrderRepository();
    }

    @Override
    public Order createOrder(Order order) {
        logger.debug("Creating order for customer: {}", order != null ? order.getCustomerName() : "null");
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
            order.setOrderNumber(orderRepository.generateUniqueOrderNumber());
        }
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    @Override
    public Order getOrderById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Order ID must be a positive number");
        }
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getAllOrders(Boolean isAdmin, int storeId) {
        return orderRepository.findAll(isAdmin,storeId);
    }

    @Override
    public Order updateOrder(Order order) {
        return orderRepository.update(order);
    }
}
