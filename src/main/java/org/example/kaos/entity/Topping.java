package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "toppings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "price")
    private Double price;
}
