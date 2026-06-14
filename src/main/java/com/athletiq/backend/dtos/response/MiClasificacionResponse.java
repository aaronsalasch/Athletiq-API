package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MiClasificacionResponse {
    private int posicion;
    private int totalParticipantes;
    private Integer xpAcumulada;
    private UUID idLiga;
    private String nombreLiga;
    private String colorHexLiga;
    // top X% — ej. posicion=5 de 100 → percentil = 5
    private int percentil;
}
