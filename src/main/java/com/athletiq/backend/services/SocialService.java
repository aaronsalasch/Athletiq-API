package com.athletiq.backend.services;

import com.athletiq.backend.dtos.response.UsuarioBusquedaResponse;

import java.util.List;
import java.util.UUID;

public interface SocialService {
    List<UsuarioBusquedaResponse> buscarUsuarios(UUID currentUserId, String query);
    void seguirUsuario(UUID currentUserId, UUID targetUserId);
    void dejarDeSeguirUsuario(UUID currentUserId, UUID targetUserId);
}
