package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Habilidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HabilidadRepository extends JpaRepository<Habilidad, UUID> {
    List<Habilidad> findBySeccionIdOrderByOrden(UUID seccionId);
}
