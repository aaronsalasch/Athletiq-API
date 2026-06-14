package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.response.HabilidadDetalleResponse;
import com.athletiq.backend.services.ActividadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Detalle de Habilidad con todos sus ejercicios y pasos — accesible en Modo Invitado.
 */
@RestController
@RequestMapping("/api/habilidades")
@RequiredArgsConstructor
public class HabilidadController {

    private final ActividadService actividadService;

    /**
     * GET /api/habilidades/{id}
     * Retorna la habilidad con la lista completa de ejercicios (incluye pasos de cada uno).
     */
    @GetMapping("/{id}")
    public ResponseEntity<HabilidadDetalleResponse> getDetalle(@PathVariable UUID id) {
        return ResponseEntity.ok(actividadService.getHabilidadDetalle(id));
    }
}
