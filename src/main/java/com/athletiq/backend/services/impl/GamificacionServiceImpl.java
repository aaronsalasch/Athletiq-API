package com.athletiq.backend.services.impl;

import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import com.athletiq.backend.repositories.*;
import com.athletiq.backend.services.EventoComunidadService;
import com.athletiq.backend.services.GamificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Motor de Gamificación.
 *
 * Orquesta: ProgresoHabilidad → TransaccionXp → puntos_xp → nivel → ClasificacionUsuario.
 * Dispara el EventPublisher de forma asíncrona al final de cada operación relevante.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GamificacionServiceImpl implements GamificacionService {

    private final UsuarioRepository usuarioRepository;
    private final HabilidadEjercicioRepository habilidadEjercicioRepository;
    private final ProgresoEjercicioRepository progresoEjercicioRepository;
    private final ProgresoHabilidadRepository progresoHabilidadRepository;
    private final TransaccionXpRepository transaccionXpRepository;
    private final ClasificacionUsuarioRepository clasificacionUsuarioRepository;
    private final TemporadaRepository temporadaRepository;
    private final EventoComunidadService eventoComunidadService;

    @Override
    public void procesarCompletitudHabilidad(UUID usuarioId, UUID habilidadId) {
        long totalEjercicios = habilidadEjercicioRepository.countByHabilidadId(habilidadId);
        long completados = progresoEjercicioRepository
                .countByIdIdUsuarioAndIdIdHabilidadAndCompletadoTrue(usuarioId, habilidadId);

        if (totalEjercicios == 0 || totalEjercicios != completados) {
            return; // habilidad aún incompleta
        }

        ProgresoHabilidadKey key = new ProgresoHabilidadKey(usuarioId, habilidadId);
        ProgresoHabilidad ph = progresoHabilidadRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("ProgresoHabilidad", habilidadId));

        if (ph.getCompletado()) {
            return; // ya procesada anteriormente (idempotencia)
        }

        ph.setCompletado(true);
        ph.setFechaCompletado(LocalDateTime.now());
        progresoHabilidadRepository.save(ph);

        // Sumar XP total de todos los ejercicios de la habilidad
        int xpTotal = habilidadEjercicioRepository
                .findByHabilidadIdOrderByOrden(habilidadId)
                .stream()
                .mapToInt(HabilidadEjercicio::getXpOtorgada)
                .sum();

        sumarXpAlUsuario(usuarioId, xpTotal, habilidadId);

        // Evento asíncrono — no bloquea la transacción actual
        eventoComunidadService.publishHabilidadCompletada(usuarioId, habilidadId);
        log.info("Habilidad {} completada para usuario {}", habilidadId, usuarioId);
    }

    @Override
    public void sumarXpAlUsuario(UUID usuarioId, int cantidad, UUID habilidadId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        // Registrar transacción de XP
        Habilidad habilidadRef = habilidadId != null
                ? habilidadEjercicioRepository
                        .findByHabilidadIdOrderByOrden(habilidadId)
                        .stream()
                        .findFirst()
                        .map(HabilidadEjercicio::getHabilidad)
                        .orElse(null)
                : null;

        transaccionXpRepository.save(TransaccionXp.builder()
                .usuario(usuario)
                .habilidad(habilidadRef)
                .cantidadXp(cantidad)
                .build());

        // Actualizar puntos y nivel
        int nivelAnterior = usuario.getNivel();
        usuario.setPuntosXp(usuario.getPuntosXp() + cantidad);
        usuario.setNivel(calcularNivel(usuario.getPuntosXp()));
        usuarioRepository.save(usuario);

        if (usuario.getNivel() > nivelAnterior) {
            eventoComunidadService.publishNivelAlcanzado(usuarioId, usuario.getNivel());
            log.info("Usuario {} subió al nivel {}", usuarioId, usuario.getNivel());
        }

        actualizarClasificacion(usuarioId, cantidad);
    }

    @Override
    public void actualizarClasificacion(UUID usuarioId, int xpGanada) {
        temporadaRepository.findByActivaTrue().ifPresent(temporada ->
            clasificacionUsuarioRepository
                    .findByUsuarioIdAndTemporadaId(usuarioId, temporada.getId())
                    .ifPresent(clasificacion -> {
                        clasificacion.setXpAcumulada(clasificacion.getXpAcumulada() + xpGanada);
                        clasificacionUsuarioRepository.save(clasificacion);
                    })
        );
    }

    @Override
    public int calcularNivel(int puntosXp) {
        // Nivel 1: 0–99 XP, Nivel 2: 100–399 XP, Nivel 3: 400–899 XP …
        return (int) Math.floor(Math.sqrt(Math.max(puntosXp, 0) / 100.0)) + 1;
    }
}
