package com.athletiq.backend.services;

import com.athletiq.backend.dtos.response.ProgresoActividadResponse;
import com.athletiq.backend.dtos.response.ProgresoHabilidadResponse;
import com.athletiq.backend.dtos.response.ProgresoResponse;

import java.util.List;
import java.util.UUID;

public interface ProgresoService {

    /**
     * Marca un ejercicio como completado para el usuario dado.
     * Internamente dispara el Motor de Rachas y el Motor de Gamificación.
     */
    ProgresoResponse completarEjercicio(UUID usuarioId, UUID habilidadId, UUID ejercicioId);

    List<ProgresoHabilidadResponse> getProgresoHabilidades(UUID usuarioId);

    List<ProgresoActividadResponse> getProgresoActividades(UUID usuarioId);

    ProgresoHabilidadResponse getProgresoHabilidad(UUID usuarioId, UUID habilidadId);
}
