package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeccionRepository extends JpaRepository<Seccion, UUID> {
    List<Seccion> findByActividadIdOrderByOrden(UUID actividadId);
    
    long countByActividadId(UUID actividadId);
}
