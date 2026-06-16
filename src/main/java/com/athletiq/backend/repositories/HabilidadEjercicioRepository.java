package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.HabilidadEjercicio;
import com.athletiq.backend.models.keys.HabilidadEjercicioKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface HabilidadEjercicioRepository extends JpaRepository<HabilidadEjercicio, HabilidadEjercicioKey> {

    List<HabilidadEjercicio> findByHabilidadIdOrderByOrden(UUID habilidadId);

    long countByHabilidadId(UUID habilidadId);

    @Query("SELECT COUNT(he) FROM HabilidadEjercicio he JOIN he.habilidad h JOIN h.seccion s WHERE s.actividad.id = :actividadId")
    long countByActividadId(@Param("actividadId") UUID actividadId);
}
