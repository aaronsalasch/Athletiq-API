package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TransaccionXpResponse {
    private UUID id;
    private Integer cantidadXp;
    private LocalDateTime fechaGanancia;
    private UUID idHabilidad;
    private String nombreHabilidad;
}
