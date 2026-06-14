package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.keys.HabilidadEjercicioKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "habilidad_ejercicio",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_habilidad_ejercicio_orden",
        columnNames = {"id_habilidad", "orden"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabilidadEjercicio {

    @EmbeddedId
    private HabilidadEjercicioKey id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idHabilidad")
    @JoinColumn(name = "id_habilidad")
    private Habilidad habilidad;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idEjercicio")
    @JoinColumn(name = "id_ejercicio")
    private Ejercicio ejercicio;

    @Column(nullable = false)
    private Integer orden;

    private Integer series;

    private Integer repeticiones;

    @Column(nullable = false)
    private Integer xpOtorgada;
}
