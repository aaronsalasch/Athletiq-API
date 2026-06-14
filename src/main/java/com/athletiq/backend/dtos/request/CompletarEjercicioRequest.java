package com.athletiq.backend.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CompletarEjercicioRequest {

    @NotNull(message = "El id de habilidad es requerido")
    private UUID habilidadId;

    @NotNull(message = "El id de ejercicio es requerido")
    private UUID ejercicioId;
}
