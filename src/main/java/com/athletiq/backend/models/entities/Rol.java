package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
