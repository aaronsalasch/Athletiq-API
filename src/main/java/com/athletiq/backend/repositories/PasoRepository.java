package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Paso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PasoRepository extends JpaRepository<Paso, UUID> {
    List<Paso> findByEjercicioIdOrderByOrden(UUID ejercicioId);
}
