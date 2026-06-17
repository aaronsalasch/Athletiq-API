package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.response.EventoComunidadResponse;
import com.athletiq.backend.models.entities.EventoComunidad;
import com.athletiq.backend.models.entities.Usuario;
import com.athletiq.backend.models.enums.TipoEvento;
import com.athletiq.backend.repositories.ActividadRepository;
import com.athletiq.backend.repositories.EventoComunidadRepository;
import com.athletiq.backend.repositories.HabilidadRepository;
import com.athletiq.backend.repositories.LigaRepository;
import com.athletiq.backend.repositories.SeccionRepository;
import com.athletiq.backend.repositories.UsuarioRepository;
import com.athletiq.backend.services.EventoComunidadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Motor de Eventos (Event Publisher).
 *
 * Todos los métodos publish* son @Async para no bloquear la transacción
 * principal del Motor de Gamificación. Cada uno abre su propia transacción.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventoComunidadServiceImpl implements EventoComunidadService {

    private final EventoComunidadRepository eventoComunidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabilidadRepository habilidadRepository;
    private final SeccionRepository seccionRepository;
    private final ActividadRepository actividadRepository;
    private final LigaRepository ligaRepository;

    @Async
    @Override
    @Transactional
    public void publishHabilidadCompletada(UUID usuarioId, UUID habilidadId) {
        try {
            guardarEvento(usuarioId, TipoEvento.HABILIDAD_COMPLETADA, habilidadId);
            log.debug("Evento HABILIDAD_COMPLETADA publicado — usuario={} habilidad={}", usuarioId, habilidadId);
        } catch (Exception ex) {
            log.error("Error publicando HABILIDAD_COMPLETADA usuario={}", usuarioId, ex);
        }
    }

    @Async
    @Override
    @Transactional
    public void publishSeccionCompletada(UUID usuarioId, UUID seccionId) {
        try {
            guardarEvento(usuarioId, TipoEvento.SECCION_COMPLETADA, seccionId);
            log.debug("Evento SECCION_COMPLETADA publicado — usuario={} seccion={}", usuarioId, seccionId);
        } catch (Exception ex) {
            log.error("Error publicando SECCION_COMPLETADA usuario={}", usuarioId, ex);
        }
    }

    @Async
    @Override
    @Transactional
    public void publishActividadCompletada(UUID usuarioId, UUID actividadId) {
        try {
            guardarEvento(usuarioId, TipoEvento.ACTIVIDAD_COMPLETADA, actividadId);
            log.debug("Evento ACTIVIDAD_COMPLETADA publicado — usuario={} actividad={}", usuarioId, actividadId);
        } catch (Exception ex) {
            log.error("Error publicando ACTIVIDAD_COMPLETADA usuario={}", usuarioId, ex);
        }
    }

    @Async
    @Override
    @Transactional
    public void publishNivelAlcanzado(UUID usuarioId, int nivelNuevo) {
        try {
            guardarEvento(usuarioId, TipoEvento.NIVEL_ALCANZADO, null);
            log.debug("Evento NIVEL_ALCANZADO nivel={} publicado — usuario={}", nivelNuevo, usuarioId);
        } catch (Exception ex) {
            log.error("Error publicando NIVEL_ALCANZADO usuario={}", usuarioId, ex);
        }
    }

    @Async
    @Override
    @Transactional
    public void publishLigaAscenso(UUID usuarioId, UUID ligaId) {
        try {
            guardarEvento(usuarioId, TipoEvento.LIGA_ASCENSO, ligaId);
            log.debug("Evento LIGA_ASCENSO publicado — usuario={} liga={}", usuarioId, ligaId);
        } catch (Exception ex) {
            log.error("Error publicando LIGA_ASCENSO usuario={}", usuarioId, ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventoComunidadResponse> getEventosComunidad(Pageable pageable) {
        return eventoComunidadRepository
                .findAllByOrderByFechaCreacionDesc(pageable)
                .map(this::toResponse);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void guardarEvento(UUID usuarioId, TipoEvento tipo, UUID referenciaId) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        EventoComunidad evento = EventoComunidad.builder()
                .usuario(usuario)
                .tipoEvento(tipo)
                .referenciaId(referenciaId)
                .build();
        eventoComunidadRepository.save(evento);
    }

    private EventoComunidadResponse toResponse(EventoComunidad evento) {
        String referenciaNombre = "";
        if (evento.getTipoEvento() == TipoEvento.HABILIDAD_COMPLETADA && evento.getReferenciaId() != null) {
            referenciaNombre = habilidadRepository.findById(evento.getReferenciaId())
                    .map(h -> h.getNombre())
                    .orElse("Habilidad");
        } else if (evento.getTipoEvento() == TipoEvento.SECCION_COMPLETADA && evento.getReferenciaId() != null) {
            referenciaNombre = seccionRepository.findById(evento.getReferenciaId())
                    .map(s -> s.getNombre())
                    .orElse("Sección");
        } else if (evento.getTipoEvento() == TipoEvento.ACTIVIDAD_COMPLETADA && evento.getReferenciaId() != null) {
            referenciaNombre = actividadRepository.findById(evento.getReferenciaId())
                    .map(a -> a.getNombre())
                    .orElse("Actividad");
        } else if (evento.getTipoEvento() == TipoEvento.LIGA_ASCENSO && evento.getReferenciaId() != null) {
            referenciaNombre = ligaRepository.findById(evento.getReferenciaId())
                    .map(l -> l.getNombre())
                    .orElse("Liga");
        } else if (evento.getTipoEvento() == TipoEvento.NIVEL_ALCANZADO) {
            referenciaNombre = String.valueOf(evento.getUsuario().getNivel());
        }

        return EventoComunidadResponse.builder()
                .id(evento.getId())
                .idUsuario(evento.getUsuario().getId())
                .nombreUsuario(evento.getUsuario().getNombre())
                .avatarUrl(evento.getUsuario().getAvatarUrl())
                .tipoEvento(evento.getTipoEvento())
                .referenciaId(evento.getReferenciaId())
                .referenciaNombre(referenciaNombre)
                .nivelUsuario(evento.getUsuario().getNivel())
                .fechaCreacion(evento.getFechaCreacion())
                .build();
    }
}
