package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UsuarioPerfilResponse {
    private UUID id;
    private String nombre;
    private String correo;
    private String avatarUrl;
    private Integer nivel;
    private Integer puntosXp;
    private Integer rachaActual;
    private String colorHexLiga;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
    private String rol;
    private long habilidadesCompletadas;
}
