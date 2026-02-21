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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "burger_variant_id")
    private BurgerVariant burgerVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_item_id")
    private ExtraItem extraItem;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "variant_name", length = 200)
    private String variantName;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice; // Precio unitario en el momento de la orden

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double subtotal; // unitPrice * quantity

    @Column(nullable = false)
    private Double total;

    @Column(name = "observations", length = 500)
    private String observations;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "orderDetail", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private List<OrderDetailTopping> orderDetailToppings = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        if (this.subtotal == null && this.unitPrice != null && this.quantity != null) {
            this.subtotal = this.unitPrice * this.quantity;
        }
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void calculateSubtotal() {
        if (unitPrice != null && quantity != null) {
            this.subtotal = unitPrice * quantity;
        }
    }
}