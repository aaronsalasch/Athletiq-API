package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.response.EventoComunidadResponse;
import com.athletiq.backend.services.EventoComunidadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feed de la comunidad (eventos globales de logros).
 * Requiere Bearer JWT.
 */
@RestController
@RequestMapping("/api/comunidad")
@RequiredArgsConstructor
public class ComunidadController {

    private final EventoComunidadService eventoComunidadService;

    /**
     * GET /api/comunidad/eventos?page=0&size=20
     * Retorna el feed paginado de eventos de la comunidad, ordenados por fecha DESC.
     *
     * Ejemplos de eventos: usuario X completó habilidad Y, usuario Z subió a Liga Oro.
     */
    @GetMapping("/eventos")
    public ResponseEntity<Page<EventoComunidadResponse>> getEventos(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(eventoComunidadService.getEventosComunidad(pageable));
    }
}
