package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.enums.Dificultad;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "habilidades",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_habilidad_seccion_orden",
        columnNames = {"id_seccion", "orden"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion", nullable = false)
    private Seccion seccion;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Dificultad dificultad;

    // tiempo estimado en minutos
    private Integer tiempoEstimado;
}
