package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.ProgresoEjercicio;
import com.athletiq.backend.models.keys.ProgresoEjercicioKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProgresoEjercicioRepository extends JpaRepository<ProgresoEjercicio, ProgresoEjercicioKey> {

    List<ProgresoEjercicio> findByIdIdUsuarioAndIdIdHabilidad(UUID idUsuario, UUID idHabilidad);

    long countByIdIdUsuarioAndIdIdHabilidadAndCompletadoTrue(UUID idUsuario, UUID idHabilidad);
}
