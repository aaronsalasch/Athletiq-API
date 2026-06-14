package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.ClasificacionUsuario;
import com.athletiq.backend.models.keys.ClasificacionUsuarioKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClasificacionUsuarioRepository extends JpaRepository<ClasificacionUsuario, ClasificacionUsuarioKey> {

    // Lista completa — usada internamente para calcular posiciones exactas
    List<ClasificacionUsuario> findByTemporadaIdOrderByXpAcumuladaDesc(UUID temporadaId);

    // Versión paginada — para endpoints de ranking
    Page<ClasificacionUsuario> findByTemporadaIdOrderByXpAcumuladaDesc(UUID temporadaId, Pageable pageable);

    Optional<ClasificacionUsuario> findByUsuarioIdAndTemporadaId(UUID usuarioId, UUID temporadaId);

    Page<ClasificacionUsuario> findByLigaIdAndTemporadaIdOrderByXpAcumuladaDesc(UUID ligaId, UUID temporadaId, Pageable pageable);
}
