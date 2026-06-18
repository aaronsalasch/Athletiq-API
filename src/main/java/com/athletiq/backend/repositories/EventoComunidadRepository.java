package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.EventoComunidad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EventoComunidadRepository extends JpaRepository<EventoComunidad, UUID> {

    Page<EventoComunidad> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    List<EventoComunidad> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);

    @Query("SELECT e FROM EventoComunidad e WHERE " +
           "e.usuario.id = :usuarioId OR " +
           "EXISTS (SELECT s FROM Seguidor s WHERE s.id.idSeguidor = :usuarioId AND s.id.idSeguido = e.usuario.id AND e.fechaCreacion >= s.fechaSeguimiento) " +
           "ORDER BY e.fechaCreacion DESC")
    List<EventoComunidad> findFeedBySeguidos(@Param("usuarioId") UUID usuarioId);
}
