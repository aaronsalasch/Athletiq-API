package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgresoResponse {
    private boolean success;
    private boolean habilidadCompletada;
    private boolean seccionCompletada;
    private boolean actividadCompletada;
    private int xpGanada;
    private boolean subioDeNivel;
    private int nuevaRacha;
    private int nuevoNivel;
    private int nuevoXp;
}
