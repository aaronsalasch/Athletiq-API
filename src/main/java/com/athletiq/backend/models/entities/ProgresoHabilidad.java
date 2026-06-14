package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "progreso_habilidad",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_progreso_habilidad_usuario",
        columnNames = {"id_usuario", "id_habilidad"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgresoHabilidad {

    @EmbeddedId
    private ProgresoHabilidadKey id;

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

    @Builder.Default
    @Column(nullable = false)
    private Boolean completado = false;

    private LocalDateTime fechaCompletado;
}
