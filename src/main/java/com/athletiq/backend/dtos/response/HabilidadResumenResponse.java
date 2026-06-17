package com.athletiq.backend.dtos.response;

import com.athletiq.backend.models.enums.Dificultad;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class HabilidadResumenResponse {
    private UUID id;
    private Integer orden;
    private String nombre;
    private String descripcion;
    private Dificultad dificultad;
    private Integer tiempoEstimado;
    private int totalEjercicios;
    private String urlImagen;
}
