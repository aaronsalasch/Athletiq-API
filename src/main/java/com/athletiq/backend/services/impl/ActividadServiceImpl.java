package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.response.*;
import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.repositories.*;
import com.athletiq.backend.services.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActividadServiceImpl implements ActividadService {

    private final ActividadRepository actividadRepository;
    private final SeccionRepository seccionRepository;
    private final HabilidadRepository habilidadRepository;
    private final HabilidadEjercicioRepository habilidadEjercicioRepository;
    private final PasoRepository pasoRepository;

    @Override
    public List<ActividadResponse> listarActividades() {
        return actividadRepository.findAll().stream()
                .map(a -> ActividadResponse.builder()
                        .id(a.getId())
                        .nombre(a.getNombre())
                        .descripcion(a.getDescripcion())
                        .totalSecciones(seccionRepository.findByActividadIdOrderByOrden(a.getId()).size())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ActividadResponse getActividad(UUID id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad", id));
        int totalSecciones = seccionRepository.findByActividadIdOrderByOrden(id).size();
        return ActividadResponse.builder()
                .id(actividad.getId())
                .nombre(actividad.getNombre())
                .descripcion(actividad.getDescripcion())
                .totalSecciones(totalSecciones)
                .build();
    }

    @Override
    public List<SeccionResponse> listarSecciones(UUID actividadId) {
        actividadRepository.findById(actividadId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad", actividadId));

        return seccionRepository.findByActividadIdOrderByOrden(actividadId).stream()
                .map(s -> SeccionResponse.builder()
                        .id(s.getId())
                        .orden(s.getOrden())
                        .nombre(s.getNombre())
                        .descripcion(s.getDescripcion())
                        .totalHabilidades(habilidadRepository.findBySeccionIdOrderByOrden(s.getId()).size())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<HabilidadResumenResponse> listarHabilidades(UUID seccionId) {
        return habilidadRepository.findBySeccionIdOrderByOrden(seccionId).stream()
                .map(h -> HabilidadResumenResponse.builder()
                        .id(h.getId())
                        .orden(h.getOrden())
                        .nombre(h.getNombre())
                        .descripcion(h.getDescripcion())
                        .dificultad(h.getDificultad())
                        .tiempoEstimado(h.getTiempoEstimado())
                        .totalEjercicios((int) habilidadEjercicioRepository.countByHabilidadId(h.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public HabilidadDetalleResponse getHabilidadDetalle(UUID habilidadId) {
        Habilidad habilidad = habilidadRepository.findById(habilidadId)
                .orElseThrow(() -> new ResourceNotFoundException("Habilidad", habilidadId));

        List<EjercicioEnHabilidadResponse> ejercicios =
                habilidadEjercicioRepository.findByHabilidadIdOrderByOrden(habilidadId)
                        .stream()
                        .map(this::toEjercicioResponse)
                        .collect(Collectors.toList());

        return HabilidadDetalleResponse.builder()
                .id(habilidad.getId())
                .seccionId(habilidad.getSeccion().getId())
                .actividadId(habilidad.getSeccion().getActividad().getId())
                .orden(habilidad.getOrden())
                .nombre(habilidad.getNombre())
                .descripcion(habilidad.getDescripcion())
                .dificultad(habilidad.getDificultad())
                .tiempoEstimado(habilidad.getTiempoEstimado())
                .ejercicios(ejercicios)
                .build();
    }

    // ── mappers ───────────────────────────────────────────────────────────────

    private EjercicioEnHabilidadResponse toEjercicioResponse(HabilidadEjercicio he) {
        Ejercicio ejercicio = he.getEjercicio();
        List<PasoResponse> pasos = pasoRepository.findByEjercicioIdOrderByOrden(ejercicio.getId())
                .stream()
                .map(p -> PasoResponse.builder()
                        .id(p.getId())
                        .orden(p.getOrden())
                        .nombre(p.getNombre())
                        .instruccion(p.getInstruccion())
                        .build())
                .collect(Collectors.toList());

        return EjercicioEnHabilidadResponse.builder()
                .idEjercicio(ejercicio.getId())
                .orden(he.getOrden())
                .nombre(ejercicio.getNombre())
                .descripcion(ejercicio.getDescripcion())
                .series(he.getSeries())
                .repeticiones(he.getRepeticiones())
                .xpOtorgada(he.getXpOtorgada())
                .pasos(pasos)
                .build();
    }
}
