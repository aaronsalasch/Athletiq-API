package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PasoResponse {
    private UUID id;
    private Integer orden;
    private String nombre;
    private String instruccion;
}
