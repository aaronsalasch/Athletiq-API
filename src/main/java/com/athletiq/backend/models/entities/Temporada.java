package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "temporadas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Temporada {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activa = false;
}
