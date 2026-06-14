package com.athletiq.backend.services;

import com.athletiq.backend.dtos.response.ActividadResponse;
import com.athletiq.backend.dtos.response.HabilidadDetalleResponse;
import com.athletiq.backend.dtos.response.HabilidadResumenResponse;
import com.athletiq.backend.dtos.response.SeccionResponse;

import java.util.List;
import java.util.UUID;

public interface ActividadService {
    List<ActividadResponse> listarActividades();
    ActividadResponse getActividad(UUID id);
    List<SeccionResponse> listarSecciones(UUID actividadId);
    List<HabilidadResumenResponse> listarHabilidades(UUID seccionId);
    HabilidadDetalleResponse getHabilidadDetalle(UUID habilidadId);
}
