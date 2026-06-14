package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SeccionResponse {
    private UUID id;
    private Integer orden;
    private String nombre;
    private String descripcion;
    private int totalHabilidades;
}
