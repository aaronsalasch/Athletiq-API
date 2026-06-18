package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.response.EventoComunidadResponse;
import com.athletiq.backend.dtos.response.UsuarioBusquedaResponse;
import com.athletiq.backend.security.SecurityUtils;
import com.athletiq.backend.services.EventoComunidadService;
import com.athletiq.backend.services.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;
    private final EventoComunidadService eventoComunidadService;
    private final SecurityUtils securityUtils;

    @GetMapping("/usuarios/buscar")
    public ResponseEntity<List<UsuarioBusquedaResponse>> buscarUsuarios(
            @RequestParam("query") String query) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(socialService.buscarUsuarios(currentUserId, query));
    }

    @PostMapping("/usuarios/{id}/seguir")
    public ResponseEntity<Void> seguirUsuario(@PathVariable("id") UUID targetUserId) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        socialService.seguirUsuario(currentUserId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/usuarios/{id}/dejar-de-seguir")
    public ResponseEntity<Void> dejarDeSeguirUsuario(@PathVariable("id") UUID targetUserId) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        socialService.dejarDeSeguirUsuario(currentUserId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/feed")
    public ResponseEntity<List<EventoComunidadResponse>> getFeed() {
        UUID currentUserId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(eventoComunidadService.getFeedSeguidos(currentUserId));
    }
}
