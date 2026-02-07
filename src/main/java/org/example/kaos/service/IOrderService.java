package org.example.kaos.service;

import org.example.kaos.entity.Order;

import java.util.List;

public interface IOrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
    List<Order> getAllOrders(Boolean isAdmin, int storeId);
    Order updateOrder(Order order);
}
