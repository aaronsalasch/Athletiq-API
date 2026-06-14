package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "secciones",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_seccion_actividad_orden",
        columnNames = {"id_actividad", "orden"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad", nullable = false)
    private Actividad actividad;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
