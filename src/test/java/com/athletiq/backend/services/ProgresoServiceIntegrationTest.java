package com.athletiq.backend.services;

import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.models.enums.Dificultad;
import com.athletiq.backend.models.keys.HabilidadEjercicioKey;
import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import com.athletiq.backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("ProgresoService — Motor de Rachas e integración de motores")
class ProgresoServiceIntegrationTest {

    @Autowired ProgresoService progresoService;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired ActividadRepository actividadRepository;
    @Autowired SeccionRepository seccionRepository;
    @Autowired HabilidadRepository habilidadRepository;
    @Autowired EjercicioRepository ejercicioRepository;
    @Autowired HabilidadEjercicioRepository habilidadEjercicioRepository;
    @Autowired ProgresoEjercicioRepository progresoEjercicioRepository;
    @Autowired ProgresoHabilidadRepository progresoHabilidadRepository;

    // Entidades reutilizadas entre tests del mismo contexto
    private Usuario usuario;
    private Habilidad habilidad;
    private Ejercicio ejercicio;

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.findByNombre("USUARIO")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("USUARIO").descripcion("Usuario").build()));

        usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Test User")
                .correo("test_" + System.nanoTime() + "@test.com")
                .password("hashed")
                .rol(rol)
                .build());

        Actividad actividad = actividadRepository.save(
                Actividad.builder().nombre("Calistenia").build());

        Seccion seccion = seccionRepository.save(Seccion.builder()
                .actividad(actividad).orden(1).nombre("Básicos").build());

        habilidad = habilidadRepository.save(Habilidad.builder()
                .seccion(seccion).orden(1).nombre("Flexiones")
                .dificultad(Dificultad.PRINCIPIANTE).tiempoEstimado(10).build());

        ejercicio = ejercicioRepository.save(
                Ejercicio.builder().nombre("Flexión estándar").build());

        habilidadEjercicioRepository.save(HabilidadEjercicio.builder()
                .id(new HabilidadEjercicioKey(habilidad.getId(), ejercicio.getId()))
                .habilidad(habilidad).ejercicio(ejercicio)
                .orden(1).series(3).repeticiones(10).xpOtorgada(50)
                .build());
    }

    // ── Streak Engine ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Primera actividad del usuario → racha = 1")
    void completarEjercicio_firstActivity_setsRachaTo1() {
        assertThat(usuario.getFechaUltimaAct()).isNull();

        progresoService.completarEjercicio(usuario.getId(), habilidad.getId(), ejercicio.getId());

        Usuario actualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(actualizado.getRachaActual()).isEqualTo(1);
        assertThat(actualizado.getFechaUltimaAct()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("Actividad al día siguiente → racha se incrementa")
    void completarEjercicio_consecutiveDay_incrementsRacha() {
        // Simular que el usuario actuó ayer
        usuario.setFechaUltimaAct(LocalDate.now().minusDays(1));
        usuario.setRachaActual(5);
        usuarioRepository.save(usuario);

        progresoService.completarEjercicio(usuario.getId(), habilidad.getId(), ejercicio.getId());

        Usuario actualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(actualizado.getRachaActual()).isEqualTo(6);
    }

    @Test
    @DisplayName("Más de 1 día sin actividad → racha se reinicia a 1")
    void completarEjercicio_gapMoreThan1Day_resetsRacha() {
        usuario.setFechaUltimaAct(LocalDate.now().minusDays(3));
        usuario.setRachaActual(10);
        usuarioRepository.save(usuario);

        progresoService.completarEjercicio(usuario.getId(), habilidad.getId(), ejercicio.getId());

        Usuario actualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(actualizado.getRachaActual()).isEqualTo(1);
    }

    @Test
    @DisplayName("Completar el mismo ejercicio dos veces → idempotente, no duplica progreso")
    void completarEjercicio_twiceForSameExercise_isIdempotent() {
        progresoService.completarEjercicio(usuario.getId(), habilidad.getId(), ejercicio.getId());
        progresoService.completarEjercicio(usuario.getId(), habilidad.getId(), ejercicio.getId());

        long count = progresoEjercicioRepository.findByIdIdUsuarioAndIdIdHabilidad(
                        usuario.getId(), habilidad.getId()).stream()
                .filter(ProgresoEjercicio::getCompletado).count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Completar único ejercicio de la habilidad → ProgresoHabilidad marcado como completado")
    void completarEjercicio_lastExercise_marksHabilidadComplete() {
        progresoService.completarEjercicio(usuario.getId(), habilidad.getId(), ejercicio.getId());

        ProgresoHabilidadKey key = new ProgresoHabilidadKey(usuario.getId(), habilidad.getId());
        ProgresoHabilidad ph = progresoHabilidadRepository.findById(key).orElseThrow();
        assertThat(ph.getCompletado()).isTrue();
        assertThat(ph.getFechaCompletado()).isNotNull();
    }
}
