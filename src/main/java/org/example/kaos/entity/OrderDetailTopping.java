package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "is_added", nullable = false)
    private Boolean isAdded;
}
