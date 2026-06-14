package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProgresoHabilidadResponse {
    private UUID idHabilidad;
    private String nombreHabilidad;
    private Boolean completado;
    private LocalDateTime fechaCompletado;
    private int totalEjercicios;
    private int ejerciciosCompletados;
}
