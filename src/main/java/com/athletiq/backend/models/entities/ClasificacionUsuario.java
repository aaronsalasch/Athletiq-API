package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.keys.ClasificacionUsuarioKey;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clasificacion_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClasificacionUsuario {

    @EmbeddedId
    private ClasificacionUsuarioKey id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idLiga")
    @JoinColumn(name = "id_liga")
    private Liga liga;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idTemporada")
    @JoinColumn(name = "id_temporada")
    private Temporada temporada;

    @Builder.Default
    @Column(nullable = false)
    private Integer xpAcumulada = 0;
}
