package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ActividadResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private int totalSecciones;
}
