package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Ejercicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EjercicioRepository extends JpaRepository<Ejercicio, UUID> {
}
