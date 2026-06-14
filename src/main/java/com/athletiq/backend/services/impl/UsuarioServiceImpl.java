package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.request.ActualizarPerfilRequest;
import com.athletiq.backend.dtos.response.TransaccionXpResponse;
import com.athletiq.backend.dtos.response.UsuarioPerfilResponse;
import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.Usuario;
import com.athletiq.backend.repositories.ProgresoHabilidadRepository;
import com.athletiq.backend.repositories.TransaccionXpRepository;
import com.athletiq.backend.repositories.UsuarioRepository;
import com.athletiq.backend.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ProgresoHabilidadRepository progresoHabilidadRepository;
    private final TransaccionXpRepository transaccionXpRepository;

    @Override
    @Transactional(readOnly = true)
    public UsuarioPerfilResponse getPerfil(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        long completadas = progresoHabilidadRepository.countByUsuarioIdAndCompletadoTrue(usuarioId);

        return toPerfilResponse(usuario, completadas);
    }

    @Override
    @Transactional
    public UsuarioPerfilResponse actualizarPerfil(UUID usuarioId, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        // Solo actualiza los campos no nulos del request (PATCH semántico)
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            usuario.setNombre(request.getNombre());
        }
        if (request.getAvatarUrl() != null) {
            usuario.setAvatarUrl(request.getAvatarUrl());
        }

        usuarioRepository.save(usuario);

        long completadas = progresoHabilidadRepository.countByUsuarioIdAndCompletadoTrue(usuarioId);
        return toPerfilResponse(usuario, completadas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransaccionXpResponse> getHistorialXp(UUID usuarioId) {
        return transaccionXpRepository.findByUsuarioIdOrderByFechaGananciaDesc(usuarioId)
                .stream()
                .map(tx -> TransaccionXpResponse.builder()
                        .id(tx.getId())
                        .cantidadXp(tx.getCantidadXp())
                        .fechaGanancia(tx.getFechaGanancia())
                        .idHabilidad(tx.getHabilidad() != null ? tx.getHabilidad().getId() : null)
                        .nombreHabilidad(tx.getHabilidad() != null ? tx.getHabilidad().getNombre() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // ── mapper ────────────────────────────────────────────────────────────────

    private UsuarioPerfilResponse toPerfilResponse(Usuario usuario, long habilidadesCompletadas) {
        return UsuarioPerfilResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .avatarUrl(usuario.getAvatarUrl())
                .nivel(usuario.getNivel())
                .puntosXp(usuario.getPuntosXp())
                .rachaActual(usuario.getRachaActual())
                .colorHexLiga(usuario.getColorHexLiga())
                .fechaRegistro(usuario.getFechaRegistro())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .rol(usuario.getRol().getNombre())
                .habilidadesCompletadas(habilidadesCompletadas)
                .build();
    }
}
