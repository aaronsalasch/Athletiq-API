package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.response.ClasificacionEntradaResponse;
import com.athletiq.backend.dtos.response.LigaResponse;
import com.athletiq.backend.dtos.response.MiClasificacionResponse;
import com.athletiq.backend.security.SecurityUtils;
import com.athletiq.backend.services.ClasificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Rankings de la temporada activa.
 * Todos los endpoints requieren Bearer JWT.
 */
@RestController
@RequestMapping("/api/clasificacion")
@RequiredArgsConstructor
public class ClasificacionController {

    private final ClasificacionService clasificacionService;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/clasificacion/ligas
     * Lista todas las ligas disponibles ordenadas de menor a mayor jerarquía.
     */
    @GetMapping("/ligas")
    public ResponseEntity<List<LigaResponse>> listarLigas() {
        return ResponseEntity.ok(clasificacionService.listarLigas());
    }

    /**
     * GET /api/clasificacion?page=0&size=20
     * Ranking global paginado de la temporada activa, ordenado por XP desc.
     */
    @GetMapping
    public ResponseEntity<Page<ClasificacionEntradaResponse>> getRankingGlobal(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(clasificacionService.getRankingGlobal(pageable));
    }

    /**
     * GET /api/clasificacion/ligas/{ligaId}?page=0&size=20
     * Ranking filtrado por liga — útil para la vista de "Mi liga".
     */
    @GetMapping("/ligas/{ligaId}")
    public ResponseEntity<Page<ClasificacionEntradaResponse>> getRankingPorLiga(
            @PathVariable UUID ligaId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(clasificacionService.getRankingPorLiga(ligaId, pageable));
    }

    /**
     * GET /api/clasificacion/mi-posicion
     * Posición, percentil y liga actual del usuario autenticado en la temporada activa.
     */
    @GetMapping("/mi-posicion")
    public ResponseEntity<MiClasificacionResponse> getMiPosicion() {
        return ResponseEntity.ok(
                clasificacionService.getMiPosicion(securityUtils.getCurrentUserId()));
    }
}
