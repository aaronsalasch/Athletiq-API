package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.response.ProgresoActividadResponse;
import com.athletiq.backend.dtos.response.ProgresoHabilidadResponse;
import com.athletiq.backend.dtos.response.ProgresoResponse;
import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.models.keys.ProgresoEjercicioKey;
import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import com.athletiq.backend.repositories.*;
import com.athletiq.backend.services.EventoComunidadService;
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
    private final ActividadRepository actividadRepository;
    private final SeccionRepository seccionRepository;
    private final GamificacionService gamificacionService;
    private final EventoComunidadService eventoComunidadService;

    @Override
    public ProgresoResponse completarEjercicio(UUID usuarioId, UUID habilidadId, UUID ejercicioId) {
        ProgresoEjercicioKey key = new ProgresoEjercicioKey(usuarioId, habilidadId, ejercicioId);

        ProgresoEjercicio progreso = progresoEjercicioRepository.findById(key)
                .orElseGet(() -> {
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
            return ProgresoResponse.builder().success(false).build();
        }

        progreso.setCompletado(true);
        progreso.setFechaCompletado(LocalDateTime.now());
        progresoEjercicioRepository.saveAndFlush(progreso);

        inicializarProgresoHabilidadSiNecesario(usuarioId, habilidadId);
        procesarRacha(usuarioId);

        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        int nivelInicial = usuario.getNivel();
        int xpGanadaTotal = 0;

        HabilidadEjercicio he = habilidadEjercicioRepository.findById(new com.athletiq.backend.models.keys.HabilidadEjercicioKey(habilidadId, ejercicioId))
                .orElseThrow();
        xpGanadaTotal += he.getXpOtorgada();

        boolean habilidadCompletada = false;
        boolean seccionCompletada = false;
        boolean actividadCompletada = false;

        long totalEjercicios = habilidadEjercicioRepository.countByHabilidadId(habilidadId);
        long completados = progresoEjercicioRepository.countByIdIdUsuarioAndIdIdHabilidadAndCompletadoTrue(usuarioId, habilidadId);

        if (totalEjercicios > 0 && totalEjercicios == completados) {
            ProgresoHabilidad ph = progresoHabilidadRepository.findById(new ProgresoHabilidadKey(usuarioId, habilidadId)).orElseThrow();
            if (!ph.getCompletado()) {
                ph.setCompletado(true);
                ph.setFechaCompletado(LocalDateTime.now());
                progresoHabilidadRepository.saveAndFlush(ph);
                
                habilidadCompletada = true;
                xpGanadaTotal += (totalEjercicios * 50);
                
                eventoComunidadService.publishHabilidadCompletada(usuarioId, habilidadId);

                UUID seccionId = ph.getHabilidad().getSeccion().getId();
                long totalHabilidades = habilidadRepository.countBySeccionId(seccionId);
                long habilidadesCompletadas = progresoHabilidadRepository.countCompletedByUsuarioAndSeccionId(usuarioId, seccionId);

                if (totalHabilidades > 0 && totalHabilidades == habilidadesCompletadas) {
                    seccionCompletada = true;
                    xpGanadaTotal += (totalHabilidades * 100);
                    
                    eventoComunidadService.publishSeccionCompletada(usuarioId, seccionId);

                    UUID actividadId = ph.getHabilidad().getSeccion().getActividad().getId();
                    long totalHabAct = habilidadRepository.countByActividadId(actividadId);
                    long compHabAct = progresoHabilidadRepository.countCompletedByUsuarioAndActividadId(usuarioId, actividadId);

                    if (totalHabAct > 0 && totalHabAct == compHabAct) {
                        actividadCompletada = true;
                        long totalSecciones = seccionRepository.countByActividadId(actividadId);
                        xpGanadaTotal += (totalSecciones * 200);
                        
                        eventoComunidadService.publishActividadCompletada(usuarioId, actividadId);
                    }
                }
            }
        }

        gamificacionService.sumarXpAlUsuario(usuarioId, xpGanadaTotal, habilidadId);
        
        Usuario usuarioActualizado = usuarioRepository.findById(usuarioId).orElseThrow();
        boolean subioDeNivel = usuarioActualizado.getNivel() > nivelInicial;

        return ProgresoResponse.builder()
                .success(true)
                .habilidadCompletada(habilidadCompletada)
                .seccionCompletada(seccionCompletada)
                .actividadCompletada(actividadCompletada)
                .xpGanada(xpGanadaTotal)
                .subioDeNivel(subioDeNivel)
                .nuevaRacha(usuarioActualizado.getRachaActual())
                .nuevoNivel(usuarioActualizado.getNivel())
                .nuevoXp(usuarioActualizado.getPuntosXp())
                .build();
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
    public List<ProgresoActividadResponse> getProgresoActividades(UUID usuarioId) {
        return actividadRepository.findAll().stream().map(actividad -> {
            long totalEjercicios = habilidadEjercicioRepository.countByActividadId(actividad.getId());
            long completados = progresoEjercicioRepository.countCompletedByUsuarioAndActividadId(usuarioId, actividad.getId());
            
            int porcentaje = 0;
            if (totalEjercicios > 0) {
                porcentaje = (int) Math.round(((double) completados / totalEjercicios) * 100);
            }
            
            return ProgresoActividadResponse.builder()
                    .idActividad(actividad.getId())
                    .nombreActividad(actividad.getNombre())
                    .progresoPorcentaje(porcentaje)
                    .build();
        }).collect(Collectors.toList());
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

        LocalDateTime fechaUltimo = progresoEjercicioRepository.findUltimaActividadPorHabilidad(usuarioId, habilidadId);

        return ProgresoHabilidadResponse.builder()
                .idHabilidad(habilidadId)
                .idSeccion(ph.getHabilidad().getSeccion().getId())
                .idActividad(ph.getHabilidad().getSeccion().getActividad().getId())
                .nombreHabilidad(ph.getHabilidad().getNombre())
                .completado(ph.getCompletado())
                .fechaCompletado(ph.getFechaCompletado())
                .fechaUltimoEjercicio(fechaUltimo)
                .totalEjercicios((int) total)
                .ejerciciosCompletados((int) completados)
                .build();
    }
}
