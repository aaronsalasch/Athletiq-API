package com.athletiq.backend.services;

import com.athletiq.backend.dtos.response.ClasificacionEntradaResponse;
import com.athletiq.backend.dtos.response.LigaResponse;
import com.athletiq.backend.dtos.response.MiClasificacionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ClasificacionService {

    /** Ranking global paginado de la temporada activa, ordenado por XP desc. */
    Page<ClasificacionEntradaResponse> getRankingGlobal(Pageable pageable);

    /** Ranking filtrado por liga en la temporada activa (paginado). */
    Page<ClasificacionEntradaResponse> getRankingPorLiga(UUID ligaId, Pageable pageable);

    /** Posición y percentil del usuario en la temporada activa. */
    MiClasificacionResponse getMiPosicion(UUID usuarioId);

    /** Lista todas las ligas ordenadas por jerarquía. */
    List<LigaResponse> listarLigas();
}
