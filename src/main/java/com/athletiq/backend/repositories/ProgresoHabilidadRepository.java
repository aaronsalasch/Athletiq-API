package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.ProgresoHabilidad;
import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProgresoHabilidadRepository extends JpaRepository<ProgresoHabilidad, ProgresoHabilidadKey> {

    List<ProgresoHabilidad> findByUsuarioId(UUID usuarioId);

    long countByUsuarioIdAndCompletadoTrue(UUID usuarioId);
}
