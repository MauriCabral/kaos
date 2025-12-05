package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "burger_variant_id")
    private BurgerVariant burgerVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_item_id")
    private ExtraItem extraItem;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "variant_name", length = 50)
    private String variantName;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice; // Precio unitario en el momento de la orden

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double subtotal; // unitPrice * quantity

    @Column(name = "observations", length = 500)
    private String observations;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "orderDetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderDetailTopping> orderDetailToppings = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        calculateSubtotal();
    }

    @PreUpdate
    public void onUpdate() {
        calculateSubtotal();
    }

    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice * quantity;
        }
    }

    public List<OrderDetailTopping> getOrderDetailToppings() {
        return orderDetailToppings;
    }

    public void setOrderDetailToppings(List<OrderDetailTopping> orderDetailToppings) {
        this.orderDetailToppings = orderDetailToppings;
        if (orderDetailToppings != null) {
            for (OrderDetailTopping topping : orderDetailToppings) {
                topping.setOrderDetail(this);
            }
        }
    }

    public void addOrderDetailTopping(OrderDetailTopping topping) {
        orderDetailToppings.add(topping);
        topping.setOrderDetail(this);
    }
}