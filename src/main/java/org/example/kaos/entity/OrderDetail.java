package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "product_type", nullable = false, length = 20)
    private String productType; // "BURGER", "EXTRA", "COMBO"

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "variant_name", length = 50)
    private String variantName;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice; // Precio unitario en el momento de la orden

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double subtotal; // unitPrice * quantity

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        calculateSubtotal();
    }

    @PreUpdate
    public void onUpdate() {
        calculateSubtotal();
    }

    private void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice * quantity;
        }
    }
}