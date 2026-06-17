package com.athletiq.backend.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "imagenes")
@Check(constraints = "((id_habilidad IS NOT NULL AND id_ejercicio IS NULL AND id_actividad IS NULL) OR " +
                      "(id_habilidad IS NULL AND id_ejercicio IS NOT NULL AND id_actividad IS NULL) OR " +
                      "(id_habilidad IS NULL AND id_ejercicio IS NULL AND id_actividad IS NOT NULL))")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_imagen", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "url_imagen", nullable = false)
    private String urlImagen;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_habilidad")
    private Habilidad habilidad;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ejercicio")
    private Ejercicio ejercicio;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad")
    private Actividad actividad;

    @PrePersist
    protected void onCreate() {
        if (fechaSubida == null) {
            fechaSubida = LocalDateTime.now();
        }
        validarRestriccionFk();
    }

    @PreUpdate
    protected void onUpdate() {
        validarRestriccionFk();
    }

    private void validarRestriccionFk() {
        int count = 0;
        if (habilidad != null) count++;
        if (ejercicio != null) count++;
        if (actividad != null) count++;

        if (count != 1) {
            throw new IllegalStateException("Una imagen debe estar asociada exactamente a una Habilidad, un Ejercicio o una Actividad (y solo a uno).");
        }
    }
}
