package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UsuarioBusquedaResponse {
    private UUID id;
    private String nombre;
    private String avatarUrl;
    private Integer nivel;
    private boolean siguiendo;
}
