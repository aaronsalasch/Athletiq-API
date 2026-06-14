package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacciones_xp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionXp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    // nullable: la XP puede ganarse fuera de una habilidad (ej. racha)
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_habilidad")
    private Habilidad habilidad;

    @Column(nullable = false)
    private Integer cantidadXp;

    @Column(nullable = false)
    private LocalDateTime fechaGanancia;

    @PrePersist
    protected void onCreate() {
        this.fechaGanancia = LocalDateTime.now();
    }
}
