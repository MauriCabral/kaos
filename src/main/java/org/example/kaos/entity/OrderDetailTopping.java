package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_detail_topping")
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

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 0;

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
        if (quantity != null && quantity > 0 && pricePerUnit != null) {
            this.totalPrice = quantity * pricePerUnit;
        } else {
            this.totalPrice = 0.0;
        }
    }
}
