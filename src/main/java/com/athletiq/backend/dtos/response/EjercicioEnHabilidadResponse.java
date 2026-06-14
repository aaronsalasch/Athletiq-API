package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class EjercicioEnHabilidadResponse {
    private UUID idEjercicio;
    private Integer orden;
    private String nombre;
    private String descripcion;
    private Integer series;
    private Integer repeticiones;
    private Integer xpOtorgada;
    private List<PasoResponse> pasos;
}
