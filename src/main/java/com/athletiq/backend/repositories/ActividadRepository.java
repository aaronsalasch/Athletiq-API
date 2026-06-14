package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Actividad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActividadRepository extends JpaRepository<Actividad, UUID> {
}
