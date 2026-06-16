package com.athletiq.backend.dtos.response;

import com.athletiq.backend.models.enums.TipoEvento;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EventoComunidadResponse {
    private UUID id;
    private UUID idUsuario;
    private String nombreUsuario;
    private String avatarUrl;
    private TipoEvento tipoEvento;
    private UUID referenciaId;
    private String referenciaNombre;
    private Integer nivelUsuario;
    private LocalDateTime fechaCreacion;
}
