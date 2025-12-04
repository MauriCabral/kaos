package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "variant_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VariantType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String name;
}