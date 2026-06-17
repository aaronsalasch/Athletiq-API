package com.athletiq.backend.services;

import com.athletiq.backend.dtos.request.ActualizarPerfilRequest;
import com.athletiq.backend.dtos.response.TransaccionXpResponse;
import com.athletiq.backend.dtos.response.UsuarioPerfilResponse;

import java.util.List;
import java.util.UUID;

public interface UsuarioService {
    UsuarioPerfilResponse getPerfil(UUID usuarioId);
    UsuarioPerfilResponse actualizarPerfil(UUID usuarioId, ActualizarPerfilRequest request);
    List<TransaccionXpResponse> getHistorialXp(UUID usuarioId);
    UsuarioPerfilResponse subirAvatar(UUID usuarioId, org.springframework.web.multipart.MultipartFile file);
}
