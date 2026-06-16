package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.ProgresoEjercicio;
import com.athletiq.backend.models.keys.ProgresoEjercicioKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProgresoEjercicioRepository extends JpaRepository<ProgresoEjercicio, ProgresoEjercicioKey> {

    List<ProgresoEjercicio> findByIdIdUsuarioAndIdIdHabilidad(UUID idUsuario, UUID idHabilidad);

    long countByIdIdUsuarioAndIdIdHabilidadAndCompletadoTrue(UUID idUsuario, UUID idHabilidad);

    @Query("SELECT COUNT(pe) FROM ProgresoEjercicio pe JOIN pe.habilidad h JOIN h.seccion s WHERE pe.usuario.id = :usuarioId AND pe.completado = true AND s.actividad.id = :actividadId")
    long countCompletedByUsuarioAndActividadId(@Param("usuarioId") UUID usuarioId, @Param("actividadId") UUID actividadId);
}
