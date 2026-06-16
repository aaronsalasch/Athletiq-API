package com.athletiq.backend.dtos.response;

import com.athletiq.backend.models.enums.Dificultad;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class HabilidadDetalleResponse {
    private UUID id;
    private UUID seccionId;
    private UUID actividadId;
    private Integer orden;
    private String nombre;
    private String descripcion;
    private Dificultad dificultad;
    private Integer tiempoEstimado;
    private List<EjercicioEnHabilidadResponse> ejercicios;
}
