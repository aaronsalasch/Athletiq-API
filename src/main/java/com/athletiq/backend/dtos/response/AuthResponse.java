package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UUID id;
    private String nombre;
    private String correo;
    private String avatarUrl;
    private Integer nivel;
    private Integer puntosXp;
    private Integer rachaActual;
    private String colorHexLiga;
    private String rol;
}
