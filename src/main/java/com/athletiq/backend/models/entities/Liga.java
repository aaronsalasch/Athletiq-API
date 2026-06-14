package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "ligas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Liga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    // 1 = más baja, N = más alta
    @Column(nullable = false, unique = true)
    private Integer ordenJerarquia;

    @Column(nullable = false, length = 7)
    private String colorHex;
}
