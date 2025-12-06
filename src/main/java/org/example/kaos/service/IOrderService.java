package org.example.kaos.service;

import org.example.kaos.entity.Order;

public interface IOrderService {
    Order createOrder(Order order);
    Order getOrderById(Long id);
}
