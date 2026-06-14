package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.response.ActividadResponse;
import com.athletiq.backend.dtos.response.SeccionResponse;
import com.athletiq.backend.services.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Catálogo público de actividades.
 * Todos los endpoints GET de este controlador son accesibles en Modo Invitado.
 */
@RestController
@RequestMapping("/api/actividades")
@RequiredArgsConstructor
public class ActividadController {

    private final ActividadService actividadService;

    /**
     * GET /api/actividades
     * Lista todas las actividades disponibles (ej. "Calistenia", "Yoga", etc.).
     */
    @GetMapping
    public ResponseEntity<List<ActividadResponse>> listar() {
        return ResponseEntity.ok(actividadService.listarActividades());
    }

    /**
     * GET /api/actividades/{id}
     * Retorna el detalle de una actividad.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActividadResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(actividadService.getActividad(id));
    }

    /**
     * GET /api/actividades/{actividadId}/secciones
     * Lista las secciones de una actividad, ordenadas por 'orden'.
     */
    @GetMapping("/{actividadId}/secciones")
    public ResponseEntity<List<SeccionResponse>> listarSecciones(@PathVariable UUID actividadId) {
        return ResponseEntity.ok(actividadService.listarSecciones(actividadId));
    }
}
