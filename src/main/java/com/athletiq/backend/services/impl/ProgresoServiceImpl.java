package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.response.ProgresoHabilidadResponse;
import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.models.keys.ProgresoEjercicioKey;
import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import com.athletiq.backend.repositories.*;
import com.athletiq.backend.services.GamificacionService;
import com.athletiq.backend.services.ProgresoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Motor de Rachas (Streak Engine) integrado aquí.
 *
 * Lógica de racha al completar ejercicio:
 *   - fechaUltimaAct == null        → rachaActual = 1
 *   - diferencia == 0 días          → ya actuó hoy, sin cambio
 *   - diferencia == 1 día           → rachaActual + 1
 *   - diferencia > 1 día            → rachaActual = 1 (racha rota)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProgresoServiceImpl implements ProgresoService {

    private final ProgresoEjercicioRepository progresoEjercicioRepository;
    private final ProgresoHabilidadRepository progresoHabilidadRepository;
    private final HabilidadEjercicioRepository habilidadEjercicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final HabilidadRepository habilidadRepository;
    private final EjercicioRepository ejercicioRepository;
    private final GamificacionService gamificacionService;

    @Override
    public void completarEjercicio(UUID usuarioId, UUID habilidadId, UUID ejercicioId) {
        ProgresoEjercicioKey key = new ProgresoEjercicioKey(usuarioId, habilidadId, ejercicioId);

        ProgresoEjercicio progreso = progresoEjercicioRepository.findById(key)
                .orElseGet(() -> {
                    // Crear registro de progreso si no existe
                    Usuario usuario   = usuarioRepository.getReferenceById(usuarioId);
                    Habilidad hab     = habilidadRepository.getReferenceById(habilidadId);
                    Ejercicio ejerc   = ejercicioRepository.getReferenceById(ejercicioId);
                    return ProgresoEjercicio.builder()
                            .id(key)
                            .usuario(usuario)
                            .habilidad(hab)
                            .ejercicio(ejerc)
                            .build();
                });

        if (progreso.getCompletado()) {
            return; // idempotente: no reprocesar si ya estaba completado
        }

        progreso.setCompletado(true);
        progreso.setFechaCompletado(LocalDateTime.now());
        progresoEjercicioRepository.save(progreso);

        // Asegurar que exista el ProgresoHabilidad raíz
        inicializarProgresoHabilidadSiNecesario(usuarioId, habilidadId);

        // Motor de Rachas
        procesarRacha(usuarioId);

        // Motor de Gamificación — verifica si completó la habilidad completa
        gamificacionService.procesarCompletitudHabilidad(usuarioId, habilidadId);
    }

    // ── Motor de Rachas ───────────────────────────────────────────────────────

    private void procesarRacha(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        LocalDate hoy = LocalDate.now();
        LocalDate ultimaAct = usuario.getFechaUltimaAct();

        if (ultimaAct == null) {
            usuario.setRachaActual(1);
        } else {
            long diasDiferencia = ChronoUnit.DAYS.between(ultimaAct, hoy);
            if (diasDiferencia == 1) {
                usuario.setRachaActual(usuario.getRachaActual() + 1);
                log.debug("Racha extendida a {} para usuario {}", usuario.getRachaActual(), usuarioId);
            } else if (diasDiferencia > 1) {
                usuario.setRachaActual(1);
                log.debug("Racha reiniciada para usuario {}", usuarioId);
            }
            // diasDiferencia == 0 → ya actuó hoy, racha intacta
        }

        usuario.setFechaUltimaAct(hoy);
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void inicializarProgresoHabilidadSiNecesario(UUID usuarioId, UUID habilidadId) {
        ProgresoHabilidadKey key = new ProgresoHabilidadKey(usuarioId, habilidadId);
        if (!progresoHabilidadRepository.existsById(key)) {
            Usuario usuario   = usuarioRepository.getReferenceById(usuarioId);
            Habilidad hab     = habilidadRepository.getReferenceById(habilidadId);
            progresoHabilidadRepository.save(ProgresoHabilidad.builder()
                    .id(key)
                    .usuario(usuario)
                    .habilidad(hab)
                    .build());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgresoHabilidadResponse> getProgresoHabilidades(UUID usuarioId) {
        return progresoHabilidadRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(ph -> buildResponse(ph, usuarioId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProgresoHabilidadResponse getProgresoHabilidad(UUID usuarioId, UUID habilidadId) {
        ProgresoHabilidadKey key = new ProgresoHabilidadKey(usuarioId, habilidadId);
        ProgresoHabilidad ph = progresoHabilidadRepository.findById(key)
                .orElseThrow(() -> new ResourceNotFoundException("ProgresoHabilidad", habilidadId));
        return buildResponse(ph, usuarioId);
    }

    private ProgresoHabilidadResponse buildResponse(ProgresoHabilidad ph, UUID usuarioId) {
        UUID habilidadId = ph.getId().getIdHabilidad();
        long total       = habilidadEjercicioRepository.countByHabilidadId(habilidadId);
        long completados = progresoEjercicioRepository
                .countByIdIdUsuarioAndIdIdHabilidadAndCompletadoTrue(usuarioId, habilidadId);

        return ProgresoHabilidadResponse.builder()
                .idHabilidad(habilidadId)
                .nombreHabilidad(ph.getHabilidad().getNombre())
                .completado(ph.getCompletado())
                .fechaCompletado(ph.getFechaCompletado())
                .totalEjercicios((int) total)
                .ejerciciosCompletados((int) completados)
                .build();
    }
}
