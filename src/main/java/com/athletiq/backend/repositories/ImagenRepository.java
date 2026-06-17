package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import java.util.List;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, UUID> {
    long countByActividadId(UUID id);
    long countByHabilidadId(UUID id);
    long countByEjercicioId(UUID id);
    
    List<Imagen> findByHabilidadId(UUID id);
    List<Imagen> findByEjercicioId(UUID id);
    List<Imagen> findByActividadId(UUID id);
}
