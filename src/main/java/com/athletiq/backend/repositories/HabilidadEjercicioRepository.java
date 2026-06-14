package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.HabilidadEjercicio;
import com.athletiq.backend.models.keys.HabilidadEjercicioKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HabilidadEjercicioRepository extends JpaRepository<HabilidadEjercicio, HabilidadEjercicioKey> {

    List<HabilidadEjercicio> findByHabilidadIdOrderByOrden(UUID habilidadId);

    long countByHabilidadId(UUID habilidadId);
}
