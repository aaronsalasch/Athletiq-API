package com.athletiq.backend.services;

import com.athletiq.backend.dtos.response.EventoComunidadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface EventoComunidadService {

    void publishHabilidadCompletada(UUID usuarioId, UUID habilidadId);

    void publishNivelAlcanzado(UUID usuarioId, int nivelNuevo);

    void publishLigaAscenso(UUID usuarioId, UUID ligaId);

    Page<EventoComunidadResponse> getEventosComunidad(Pageable pageable);
}
