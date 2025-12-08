package org.example.kaos.service;

import org.example.kaos.entity.OrderDetail;
import org.example.kaos.entity.OrderDetailTopping;

import java.util.List;

public interface IOrderDetailService {
    OrderDetail saveOrderDetail(OrderDetail orderDetail);
    List<OrderDetail> saveOrderDetails(List<OrderDetail> orderDetails);
    void saveOrderDetailWithToppings(OrderDetail orderDetail, List<OrderDetailTopping> toppings);
    List<OrderDetail> orderDetailFindByIdOrder(Long orderId);
}
