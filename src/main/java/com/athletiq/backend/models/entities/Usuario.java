package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;

    @Builder.Default
    @Column(nullable = false)
    private Integer nivel = 1;

    @Builder.Default
    @Column(nullable = false)
    private Integer puntosXp = 0;

    private String colorHexLiga;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    private LocalDateTime ultimoAcceso;

    @Builder.Default
    @Column(nullable = false)
    private Integer rachaActual = 0;

    private LocalDate fechaUltimaAct;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
