package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RolRepository extends JpaRepository<Rol, UUID> {
    Optional<Rol> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
