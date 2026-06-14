package com.athletiq.backend.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LigaResponse {
    private UUID id;
    private String nombre;
    private Integer ordenJerarquia;
    private String colorHex;
}
