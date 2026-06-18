package com.athletiq.backend.services.impl;

import com.athletiq.backend.dtos.response.UsuarioBusquedaResponse;
import com.athletiq.backend.exceptions.ResourceNotFoundException;
import com.athletiq.backend.models.entities.Seguidor;
import com.athletiq.backend.models.entities.Usuario;
import com.athletiq.backend.models.keys.SeguidorKey;
import com.athletiq.backend.repositories.SeguidorRepository;
import com.athletiq.backend.repositories.UsuarioRepository;
import com.athletiq.backend.services.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final UsuarioRepository usuarioRepository;
    private final SeguidorRepository seguidorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioBusquedaResponse> buscarUsuarios(UUID currentUserId, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String formattedQuery = "%" + query.trim() + "%";
        List<Usuario> matched = usuarioRepository.findByNombreContainingIgnoreCaseAndAccents(formattedQuery);
        return matched.stream()
                .map(u -> {
                    SeguidorKey key = new SeguidorKey(currentUserId, u.getId());
                    boolean siguiendo = seguidorRepository.existsById(key);
                    return UsuarioBusquedaResponse.builder()
                            .id(u.getId())
                            .nombre(u.getNombre())
                            .avatarUrl(u.getAvatarUrl())
                            .nivel(u.getNivel())
                            .siguiendo(siguiendo)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void seguirUsuario(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalStateException("Un usuario no puede seguirse a sí mismo");
        }

        Usuario seguidor = usuarioRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", currentUserId));
        Usuario seguido = usuarioRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", targetUserId));

        SeguidorKey key = new SeguidorKey(currentUserId, targetUserId);

        if (seguidorRepository.existsById(key)) {
            return; // Ya lo sigue
        }

        Seguidor nuevoSeguidor = Seguidor.builder()
                .id(key)
                .seguidor(seguidor)
                .seguido(seguido)
                .fechaSeguimiento(LocalDateTime.now())
                .build();

        seguidorRepository.save(nuevoSeguidor);
    }

    @Override
    @Transactional
    public void dejarDeSeguirUsuario(UUID currentUserId, UUID targetUserId) {
        SeguidorKey key = new SeguidorKey(currentUserId, targetUserId);
        if (seguidorRepository.existsById(key)) {
            seguidorRepository.deleteById(key);
        }
    }
}
