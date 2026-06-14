package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.EventoComunidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoComunidadRepository extends JpaRepository<EventoComunidad, UUID> {

    Page<EventoComunidad> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    List<EventoComunidad> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);
}
