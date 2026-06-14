package com.athletiq.backend.models.keys;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgresoEjercicioKey implements Serializable {

    @Column(name = "id_usuario", nullable = false)
    private UUID idUsuario;

    @Column(name = "id_habilidad", nullable = false)
    private UUID idHabilidad;

    @Column(name = "id_ejercicio", nullable = false)
    private UUID idEjercicio;
}
