package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.TransaccionXp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransaccionXpRepository extends JpaRepository<TransaccionXp, UUID> {
    List<TransaccionXp> findByUsuarioIdOrderByFechaGananciaDesc(UUID usuarioId);
    long countByUsuarioId(UUID usuarioId);
}
