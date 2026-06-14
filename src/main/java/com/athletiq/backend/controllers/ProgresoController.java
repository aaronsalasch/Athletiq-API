package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.request.CompletarEjercicioRequest;
import com.athletiq.backend.dtos.response.ProgresoHabilidadResponse;
import com.athletiq.backend.security.SecurityUtils;
import com.athletiq.backend.services.ProgresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Progreso del usuario autenticado.
 * Todos los endpoints requieren Bearer JWT.
 */
@RestController
@RequestMapping("/api/progreso")
@RequiredArgsConstructor
public class ProgresoController {

    private final ProgresoService progresoService;
    private final SecurityUtils securityUtils;

    /**
     * POST /api/progreso/completar
     * Marca un ejercicio como completado para el usuario en sesión.
     * Dispara internamente: Motor de Rachas → Motor de Gamificación → Eventos.
     *
     * Body: { "habilidadId": "...", "ejercicioId": "..." }
     */
    @PostMapping("/completar")
    public ResponseEntity<Void> completarEjercicio(@Valid @RequestBody CompletarEjercicioRequest request) {
        UUID usuarioId = securityUtils.getCurrentUserId();
        progresoService.completarEjercicio(usuarioId, request.getHabilidadId(), request.getEjercicioId());
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/progreso/habilidades
     * Lista el progreso de todas las habilidades iniciadas por el usuario.
     */
    @GetMapping("/habilidades")
    public ResponseEntity<List<ProgresoHabilidadResponse>> getProgresoHabilidades() {
        UUID usuarioId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(progresoService.getProgresoHabilidades(usuarioId));
    }

    /**
     * GET /api/progreso/habilidades/{habilidadId}
     * Retorna el progreso detallado de una habilidad específica.
     */
    @GetMapping("/habilidades/{habilidadId}")
    public ResponseEntity<ProgresoHabilidadResponse> getProgresoHabilidad(@PathVariable UUID habilidadId) {
        UUID usuarioId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(progresoService.getProgresoHabilidad(usuarioId, habilidadId));
    }
}
