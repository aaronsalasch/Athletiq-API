package com.athletiq.backend.config;

import com.athletiq.backend.models.entities.Liga;
import com.athletiq.backend.models.entities.Rol;
import com.athletiq.backend.models.entities.Temporada;
import com.athletiq.backend.repositories.LigaRepository;
import com.athletiq.backend.repositories.RolRepository;
import com.athletiq.backend.repositories.TemporadaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Siembra los datos mínimos necesarios para que la aplicación arranque correctamente:
 * roles, ligas y la primera temporada activa.
 *
 * Cada bloque es idempotente — solo inserta si el registro no existe.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RolRepository rolRepository;
    private final LigaRepository ligaRepository;
    private final TemporadaRepository temporadaRepository;

    @Bean
    @Transactional
    public CommandLineRunner seedData() {
        return args -> {
            seedRoles();
            seedLigas();
            seedTemporadaInicial();
        };
    }

    // ── Roles ────────────────────────────────────────────────────────────────

    private void seedRoles() {
        seedRol("USUARIO",  "Usuario estándar de la plataforma");
        seedRol("ADMIN",    "Administrador con acceso total");
        seedRol("INVITADO", "Acceso de solo lectura sin cuenta");
    }

    private void seedRol(String nombre, String descripcion) {
        if (!rolRepository.existsByNombre(nombre)) {
            rolRepository.save(Rol.builder().nombre(nombre).descripcion(descripcion).build());
            log.info("Rol '{}' creado.", nombre);
        }
    }

    // ── Ligas ────────────────────────────────────────────────────────────────

    private void seedLigas() {
        List<LigaSeed> ligas = List.of(
            new LigaSeed("Bronce",   1, "#CD7F32"),
            new LigaSeed("Plata",    2, "#C0C0C0"),
            new LigaSeed("Oro",      3, "#FFD700"),
            new LigaSeed("Platino",  4, "#00CED1"),
            new LigaSeed("Diamante", 5, "#B9F2FF")
        );

        for (LigaSeed seed : ligas) {
            if (ligaRepository.findByOrdenJerarquia(seed.orden()).isEmpty()) {
                ligaRepository.save(Liga.builder()
                        .nombre(seed.nombre())
                        .ordenJerarquia(seed.orden())
                        .colorHex(seed.colorHex())
                        .build());
                log.info("Liga '{}' creada (orden={}).", seed.nombre(), seed.orden());
            }
        }
    }

    // ── Temporada inicial ─────────────────────────────────────────────────────

    private void seedTemporadaInicial() {
        if (temporadaRepository.findByActivaTrue().isEmpty()) {
            Temporada temporada = Temporada.builder()
                    .fechaInicio(LocalDate.now())
                    .activa(true)
                    .build();
            temporadaRepository.save(temporada);
            log.info("Primera temporada creada con inicio {}.", temporada.getFechaInicio());
        }
    }

    // ── Registro interno ─────────────────────────────────────────────────────

    private record LigaSeed(String nombre, int orden, String colorHex) {}
}
