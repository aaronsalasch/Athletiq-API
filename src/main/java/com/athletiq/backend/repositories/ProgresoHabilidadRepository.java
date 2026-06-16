package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.ProgresoHabilidad;
import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProgresoHabilidadRepository extends JpaRepository<ProgresoHabilidad, ProgresoHabilidadKey> {

    List<ProgresoHabilidad> findByUsuarioId(UUID usuarioId);

    long countByUsuarioIdAndCompletadoTrue(UUID usuarioId);

    @Query("SELECT COUNT(ph) FROM ProgresoHabilidad ph JOIN ph.habilidad h JOIN h.seccion s WHERE ph.usuario.id = :usuarioId AND ph.completado = true AND s.actividad.id = :actividadId")
    long countCompletedByUsuarioAndActividadId(@Param("usuarioId") UUID usuarioId, @Param("actividadId") UUID actividadId);

    @Query("SELECT COUNT(ph) FROM ProgresoHabilidad ph JOIN ph.habilidad h WHERE ph.usuario.id = :usuarioId AND ph.completado = true AND h.seccion.id = :seccionId")
    long countCompletedByUsuarioAndSeccionId(@Param("usuarioId") UUID usuarioId, @Param("seccionId") UUID seccionId);
}
