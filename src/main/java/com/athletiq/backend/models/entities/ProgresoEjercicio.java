package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.keys.ProgresoEjercicioKey;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "progreso_ejercicio",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_progreso_ejercicio_usuario",
        columnNames = {"id_usuario", "id_habilidad", "id_ejercicio"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgresoEjercicio {

    @EmbeddedId
    private ProgresoEjercicioKey id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

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

    @Builder.Default
    @Column(nullable = false)
    private Boolean completado = false;

    private LocalDateTime fechaCompletado;
}
