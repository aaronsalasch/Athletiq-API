package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Temporada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemporadaRepository extends JpaRepository<Temporada, UUID> {
    Optional<Temporada> findByActivaTrue();
}
