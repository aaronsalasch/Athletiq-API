package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.response.ClasificacionEntradaResponse;
import com.athletiq.backend.dtos.response.LigaResponse;
import com.athletiq.backend.dtos.response.MiClasificacionResponse;
import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.ClasificacionUsuario;
import com.athletiq.backend.repositories.ClasificacionUsuarioRepository;
import com.athletiq.backend.repositories.LigaRepository;
import com.athletiq.backend.repositories.TemporadaRepository;
import com.athletiq.backend.services.ClasificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClasificacionServiceImpl implements ClasificacionService {

    private final ClasificacionUsuarioRepository clasificacionUsuarioRepository;
    private final TemporadaRepository temporadaRepository;
    private final LigaRepository ligaRepository;

    @Override
    public Page<ClasificacionEntradaResponse> getRankingGlobal(Pageable pageable) {
        return temporadaRepository.findByActivaTrue()
                .map(temporada -> {
                    Page<ClasificacionUsuario> page = clasificacionUsuarioRepository
                            .findByTemporadaIdOrderByXpAcumuladaDesc(temporada.getId(), pageable);
                    // offset para que posicion=1 en la primera página, 21 en la segunda (size=20), etc.
                    long offset = pageable.getOffset();
                    return page.map(cu -> toEntrada(cu, (int) offset + page.getContent().indexOf(cu) + 1));
                })
                .orElse(Page.empty());
    }

    @Override
    public Page<ClasificacionEntradaResponse> getRankingPorLiga(UUID ligaId, Pageable pageable) {
        return temporadaRepository.findByActivaTrue()
                .map(temporada -> {
                    Page<ClasificacionUsuario> page = clasificacionUsuarioRepository
                            .findByLigaIdAndTemporadaIdOrderByXpAcumuladaDesc(ligaId, temporada.getId(), pageable);
                    long offset = pageable.getOffset();
                    return page.map(cu -> toEntrada(cu, (int) offset + page.getContent().indexOf(cu) + 1));
                })
                .orElse(Page.empty());
    }

    @Override
    public MiClasificacionResponse getMiPosicion(UUID usuarioId) {
        var temporada = temporadaRepository.findByActivaTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Temporada activa"));

        List<ClasificacionUsuario> rankingGlobal =
                clasificacionUsuarioRepository.findByTemporadaIdOrderByXpAcumuladaDesc(temporada.getId());

        int total = rankingGlobal.size();
        int posicion = -1;
        ClasificacionUsuario miClasificacion = null;

        for (int i = 0; i < total; i++) {
            if (rankingGlobal.get(i).getUsuario().getId().equals(usuarioId)) {
                posicion = i + 1; // 1-indexed
                miClasificacion = rankingGlobal.get(i);
                break;
            }
        }

        if (miClasificacion == null) {
            throw new ResourceNotFoundException("Clasificación del usuario en la temporada activa");
        }

        // percentil = % de usuarios que el usuario actual supera
        int percentil = total > 1 ? (int) (((double)(total - posicion) / (total - 1)) * 100) : 100;

        return MiClasificacionResponse.builder()
                .posicion(posicion)
                .totalParticipantes(total)
                .xpAcumulada(miClasificacion.getXpAcumulada())
                .idLiga(miClasificacion.getLiga().getId())
                .nombreLiga(miClasificacion.getLiga().getNombre())
                .colorHexLiga(miClasificacion.getLiga().getColorHex())
                .percentil(percentil)
                .build();
    }

    @Override
    public List<LigaResponse> listarLigas() {
        return ligaRepository.findAllByOrderByOrdenJerarquiaAsc().stream()
                .map(liga -> LigaResponse.builder()
                        .id(liga.getId())
                        .nombre(liga.getNombre())
                        .ordenJerarquia(liga.getOrdenJerarquia())
                        .colorHex(liga.getColorHex())
                        .build())
                .collect(Collectors.toList());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ClasificacionEntradaResponse toEntrada(ClasificacionUsuario cu, int posicion) {
        return ClasificacionEntradaResponse.builder()
                .posicion(posicion)
                .idUsuario(cu.getUsuario().getId())
                .nombre(cu.getUsuario().getNombre())
                .avatarUrl(cu.getUsuario().getAvatarUrl())
                .nivel(cu.getUsuario().getNivel())
                .xpAcumulada(cu.getXpAcumulada())
                .nombreLiga(cu.getLiga().getNombre())
                .colorHexLiga(cu.getLiga().getColorHex())
                .build();
    }
}
