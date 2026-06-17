package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Habilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HabilidadRepository extends JpaRepository<Habilidad, UUID> {
    List<Habilidad> findBySeccionIdOrderByOrden(UUID seccionId);

    long countBySeccionId(UUID seccionId);

    @Query("SELECT COUNT(h) FROM Habilidad h JOIN h.seccion s WHERE s.actividad.id = :actividadId")
    long countByActividadId(@Param("actividadId") UUID actividadId);

    @Query("SELECT h FROM Habilidad h JOIN FETCH h.seccion s JOIN FETCH s.actividad a")
    List<Habilidad> findAllWithSeccionAndActividad();
}
