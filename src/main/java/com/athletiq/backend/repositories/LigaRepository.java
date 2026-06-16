package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Liga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LigaRepository extends JpaRepository<Liga, UUID> {

    List<Liga> findAllByOrderByOrdenJerarquiaAsc();
    Optional<Liga> findByOrdenJerarquia(Integer orden);
    Optional<Liga> findByNombre(String nombre);
}
