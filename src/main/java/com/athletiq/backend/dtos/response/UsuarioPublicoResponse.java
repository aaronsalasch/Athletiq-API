package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UsuarioPublicoResponse {
    private UUID id;
    private String nombre;
    private String avatarUrl;
    private Integer nivel;
    private Integer puntosXp;
    private Integer rachaActual;
    private String colorHexLiga;
    private long habilidadesCompletadas;
    private boolean siguiendo;
    private long seguidoresCount;
    private long seguidosCount;
    private List<ActividadResponse> actividadesFavoritas;
    private List<EventoComunidadResponse> eventos;
}
