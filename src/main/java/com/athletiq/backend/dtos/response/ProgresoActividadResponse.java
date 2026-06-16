package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProgresoActividadResponse {
    private UUID idActividad;
    private String nombreActividad;
    private Integer progresoPorcentaje;
}
