package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "pasos",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_paso_ejercicio_orden",
        columnNames = {"id_ejercicio", "orden"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ejercicio", nullable = false)
    private Ejercicio ejercicio;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String instruccion;
}
