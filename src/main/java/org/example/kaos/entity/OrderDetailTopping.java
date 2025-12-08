package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_detail_toppings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailTopping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_detail_id", nullable = false)
    private OrderDetail orderDetail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "topping_id", nullable = false)
    private Topping topping;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "burger_variant_id")
    private BurgerVariant burgerVariant;

    @Column(name = "is_added", nullable = false)
    private Boolean isAdded;

    @Column(name = "price_per_unit")
    private Double pricePerUnit;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void calculateTotalPrice() {
        if (Boolean.TRUE.equals(isAdded) && pricePerUnit != null) {
            this.totalPrice = pricePerUnit;
        } else {
            this.totalPrice = 0.0; // quitar topping = 0
        }
    }
}
