package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "burger_variants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BurgerVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "burger_id", nullable = false)
    private Burger burger;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "variant_type_id", nullable = false)
    private VariantType variantType;

    @Column(nullable = false)
    private Double price;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "created_by_user")
    private Long createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}