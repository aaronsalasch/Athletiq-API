package com.athletiq.backend.controllers;

import com.athletiq.backend.dtos.request.ActualizarPerfilRequest;
import com.athletiq.backend.dtos.response.TransaccionXpResponse;
import com.athletiq.backend.dtos.response.UsuarioPerfilResponse;
import com.athletiq.backend.security.SecurityUtils;
import com.athletiq.backend.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión del perfil del usuario autenticado.
 * Todos los endpoints requieren Bearer JWT.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SecurityUtils securityUtils;

    /**
     * GET /api/usuarios/me
     * Retorna el perfil completo del usuario en sesión:
     * datos personales, nivel, XP, racha, liga y habilidades completadas.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioPerfilResponse> getMiPerfil() {
        return ResponseEntity.ok(usuarioService.getPerfil(securityUtils.getCurrentUserId()));
    }

    /**
     * PUT /api/usuarios/me
     * Actualiza nombre y/o avatar del usuario en sesión.
     * Los campos no enviados (null) se ignoran — comportamiento PATCH semántico.
     *
     * Body: { "nombre": "...", "avatarUrl": "..." }
     */
    @PutMapping("/me")
    public ResponseEntity<UsuarioPerfilResponse> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilRequest request) {
        return ResponseEntity.ok(
                usuarioService.actualizarPerfil(securityUtils.getCurrentUserId(), request));
    }

    /**
     * POST /api/usuarios/me/avatar
     * Sube un archivo de imagen como foto de perfil para el usuario en sesión.
     */
    @PostMapping("/me/avatar")
    public ResponseEntity<UsuarioPerfilResponse> subirFotoPerfil(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(
                usuarioService.subirAvatar(securityUtils.getCurrentUserId(), file));
    }

    /**
     * GET /api/usuarios/me/xp
     * Historial de transacciones de XP del usuario, ordenado por fecha DESC.
     */
    @GetMapping("/me/xp")
    public ResponseEntity<List<TransaccionXpResponse>> getMiHistorialXp() {
        return ResponseEntity.ok(usuarioService.getHistorialXp(securityUtils.getCurrentUserId()));
    }
}
