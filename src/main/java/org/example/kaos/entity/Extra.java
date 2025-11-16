package org.example.kaos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "extras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Extra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, length = 200)
    private String description;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data", columnDefinition = "BYTEA")
    private byte[] imageData;

    @Column(nullable = false)
    private Double price;

    @Column(name = "created_by_user")
    private Long createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
