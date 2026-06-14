package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.enums.TipoEvento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eventos_comunidad")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoComunidad {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoEvento tipoEvento;

    // ID flexible hacia la entidad referenciada (habilidad, liga, etc.)
    @Column(columnDefinition = "UUID")
    private UUID referenciaId;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
