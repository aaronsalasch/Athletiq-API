package com.athletiq.backend.security;

import com.athletiq.backend.exceptions.UnauthorizedException;
import com.athletiq.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No hay sesión activa");
        }
        String correo = auth.getName();
        return usuarioRepository.findByCorreo(correo)
                .map(u -> u.getId())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado en sesión"));
    }

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No hay sesión activa");
        }
        return auth.getName();
    }
}
