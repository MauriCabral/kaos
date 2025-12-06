package org.example.kaos.service.implementation;

import org.example.kaos.entity.OrderDetail;
import org.example.kaos.entity.OrderDetailTopping;
import org.example.kaos.repository.OrderDetailRepository;
import org.example.kaos.repository.OrderDetailToppingRepository;
import org.example.kaos.service.IOrderDetailService;

import java.util.List;

public class OrderDetailServiceImpl implements IOrderDetailService {
    private final OrderDetailRepository orderDetailRepository;
    private final OrderDetailToppingRepository orderDetailToppingRepository;

    public OrderDetailServiceImpl() {
        this.orderDetailRepository = new OrderDetailRepository();
        this.orderDetailToppingRepository = new OrderDetailToppingRepository();
    }

    @Override
    public OrderDetail saveOrderDetail(OrderDetail orderDetail) {
        OrderDetail savedDetail = orderDetailRepository.save(orderDetail);

        if (orderDetail.getOrderDetailToppings() != null &&
                !orderDetail.getOrderDetailToppings().isEmpty()) {

            for (OrderDetailTopping topping : orderDetail.getOrderDetailToppings()) {
                topping.setOrderDetail(savedDetail);
                if (savedDetail.getBurgerVariant() != null) {
                    topping.setBurgerVariant(savedDetail.getBurgerVariant());
                }
                orderDetailToppingRepository.save(topping);
            }
        }

        return savedDetail;
    }

    @Override
    public List<OrderDetail> saveOrderDetails(List<OrderDetail> orderDetails) {
        return orderDetailRepository.saveAll(orderDetails);
    }

    @Override
    public void saveOrderDetailWithToppings(OrderDetail orderDetail, List<OrderDetailTopping> toppings) {
        OrderDetail savedDetail = orderDetailRepository.save(orderDetail);

        for (OrderDetailTopping topping : toppings) {
            topping.setOrderDetail(savedDetail);
            orderDetailToppingRepository.save(topping);
        }
    }
}
