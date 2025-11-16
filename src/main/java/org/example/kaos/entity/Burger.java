package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "burgers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Burger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 2)
    private String code;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data", columnDefinition = "BYTEA")
    private byte[] imageData;

    @Column(name = "created_by_user")
    private Long createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "burger", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BurgerVariant> variants = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void addVariant(BurgerVariant variant) {
        variants.add(variant);
        variant.setBurger(this);
    }
}