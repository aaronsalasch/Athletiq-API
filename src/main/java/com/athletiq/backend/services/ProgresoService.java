package com.athletiq.backend.services;

import com.athletiq.backend.dtos.response.ProgresoHabilidadResponse;

import java.util.List;
import java.util.UUID;

public interface ProgresoService {

    /**
     * Marca un ejercicio como completado para el usuario dado.
     * Internamente dispara el Motor de Rachas y el Motor de Gamificación.
     */
    void completarEjercicio(UUID usuarioId, UUID habilidadId, UUID ejercicioId);

    List<ProgresoHabilidadResponse> getProgresoHabilidades(UUID usuarioId);

    ProgresoHabilidadResponse getProgresoHabilidad(UUID usuarioId, UUID habilidadId);
}
