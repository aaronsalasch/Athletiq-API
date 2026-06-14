package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ClasificacionEntradaResponse {
    private int posicion;
    private UUID idUsuario;
    private String nombre;
    private String avatarUrl;
    private Integer nivel;
    private Integer xpAcumulada;
    private String nombreLiga;
    private String colorHexLiga;
}
