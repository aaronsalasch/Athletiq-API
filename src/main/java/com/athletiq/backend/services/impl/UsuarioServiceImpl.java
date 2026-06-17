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

    @Override
    @Transactional
    public UsuarioPerfilResponse subirAvatar(UUID usuarioId, org.springframework.web.multipart.MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", usuarioId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        try {
            java.io.File uploadDir = new java.io.File("./uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            java.nio.file.Path path = java.nio.file.Paths.get("./uploads/" + fileName);
            java.nio.file.Files.write(path, file.getBytes());

            String fileUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            usuario.setAvatarUrl(fileUrl);
            usuarioRepository.save(usuario);

            long completadas = progresoHabilidadRepository.countByUsuarioIdAndCompletadoTrue(usuarioId);
            return toPerfilResponse(usuario, completadas);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al guardar la foto de perfil", e);
        }
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
