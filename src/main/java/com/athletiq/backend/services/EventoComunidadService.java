package com.athletiq.backend.services;

import com.athletiq.backend.dtos.response.EventoComunidadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventoComunidadService {

    void publishHabilidadCompletada(UUID usuarioId, UUID habilidadId);
    void publishSeccionCompletada(UUID usuarioId, UUID seccionId);
    void publishActividadCompletada(UUID usuarioId, UUID actividadId);

    void publishNivelAlcanzado(UUID usuarioId, int nivelNuevo);

    void publishLigaAscenso(UUID usuarioId, UUID ligaId);

    Page<EventoComunidadResponse> getEventosComunidad(Pageable pageable);

    java.util.List<EventoComunidadResponse> getFeedSeguidos(java.util.UUID usuarioId);

    java.util.List<EventoComunidadResponse> getEventosUsuario(java.util.UUID usuarioId);
}
