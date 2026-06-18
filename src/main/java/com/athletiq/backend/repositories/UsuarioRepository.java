package com.athletiq.backend.repositories;

import com.athletiq.backend.models.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    java.util.List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT * FROM usuarios WHERE translate(lower(nombre), '\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc', 'aeiouu') LIKE translate(lower(:nombre), '\u00e1\u00e9\u00ed\u00f3\u00fa\u00fc', 'aeiouu')",
        nativeQuery = true
    )
    java.util.List<Usuario> findByNombreContainingIgnoreCaseAndAccents(@org.springframework.data.repository.query.Param("nombre") String nombre);
}
