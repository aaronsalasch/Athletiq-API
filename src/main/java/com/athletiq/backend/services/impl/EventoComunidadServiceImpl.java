package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.response.EventoComunidadResponse;
import com.athletiq.backend.models.entities.EventoComunidad;
import com.athletiq.backend.models.entities.Usuario;
import com.athletiq.backend.models.enums.TipoEvento;
import com.athletiq.backend.repositories.EventoComunidadRepository;
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
        return EventoComunidadResponse.builder()
                .id(evento.getId())
                .idUsuario(evento.getUsuario().getId())
                .nombreUsuario(evento.getUsuario().getNombre())
                .avatarUrl(evento.getUsuario().getAvatarUrl())
                .tipoEvento(evento.getTipoEvento())
                .referenciaId(evento.getReferenciaId())
                .fechaCreacion(evento.getFechaCreacion())
                .build();
    }
}
