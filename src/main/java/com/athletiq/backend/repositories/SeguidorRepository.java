package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Seguidor;
import com.athletiq.backend.models.keys.SeguidorKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SeguidorRepository extends JpaRepository<Seguidor, SeguidorKey> {
    long countByIdIdSeguidor(UUID idSeguidor);
    long countByIdIdSeguido(UUID idSeguido);
}
