package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.response.HabilidadResumenResponse;
import com.athletiq.backend.services.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Sub-recursos de Seccion — accesibles en Modo Invitado (GET público).
 */
@RestController
@RequestMapping("/api/secciones")
@RequiredArgsConstructor
public class SeccionController {

    private final ActividadService actividadService;

    /**
     * GET /api/secciones/{seccionId}/habilidades
     * Lista las habilidades de una sección, ordenadas por 'orden'.
     */
    @GetMapping("/{seccionId}/habilidades")
    public ResponseEntity<List<HabilidadResumenResponse>> listarHabilidades(@PathVariable UUID seccionId) {
        return ResponseEntity.ok(actividadService.listarHabilidades(seccionId));
    }
}
