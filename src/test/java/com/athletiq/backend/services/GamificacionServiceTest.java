package com.athletiq.backend.services;

import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.models.enums.Dificultad;
import com.athletiq.backend.models.keys.ClasificacionUsuarioKey;
import com.athletiq.backend.models.keys.HabilidadEjercicioKey;
import com.athletiq.backend.models.keys.ProgresoEjercicioKey;
import com.athletiq.backend.models.keys.ProgresoHabilidadKey;
import com.athletiq.backend.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("GamificacionService — Motor de Gamificación")
class GamificacionServiceTest {

    @Autowired GamificacionService gamificacionService;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired ActividadRepository actividadRepository;
    @Autowired SeccionRepository seccionRepository;
    @Autowired HabilidadRepository habilidadRepository;
    @Autowired EjercicioRepository ejercicioRepository;
    @Autowired HabilidadEjercicioRepository habilidadEjercicioRepository;
    @Autowired TransaccionXpRepository transaccionXpRepository;
    @Autowired ClasificacionUsuarioRepository clasificacionUsuarioRepository;
    @Autowired LigaRepository ligaRepository;
    @Autowired TemporadaRepository temporadaRepository;
    @Autowired ProgresoHabilidadRepository progresoHabilidadRepository;
    @Autowired ProgresoEjercicioRepository progresoEjercicioRepository;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        Rol rol = rolRepository.findByNombre("USUARIO")
                .orElseGet(() -> rolRepository.save(Rol.builder().nombre("USUARIO").descripcion("test").build()));

        usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Gamif Test")
                .correo("gamif_" + System.nanoTime() + "@test.com")
                .password("hashed")
                .rol(rol)
                .build());
    }

    // ── Fórmula de nivel ──────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0} XP → nivel {1}")
    @CsvSource({
        "0,   1",
        "99,  1",
        "100, 2",
        "399, 2",
        "400, 3",
        "899, 3",
        "900, 4"
    })
    @DisplayName("calcularNivel — fórmula cuadrática correcta")
    void calcularNivel_formula(int xp, int nivelEsperado) {
        assertThat(gamificacionService.calcularNivel(xp)).isEqualTo(nivelEsperado);
    }

    // ── sumarXpAlUsuario ─────────────────────────────────────────────────────

    @Test
    @DisplayName("sumarXpAlUsuario → actualiza puntosXp del usuario en BD")
    void sumarXp_updatesUsuarioPuntos() {
        gamificacionService.sumarXpAlUsuario(usuario.getId(), 150, null);

        Usuario actualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(actualizado.getPuntosXp()).isEqualTo(150);
    }

    @Test
    @DisplayName("sumarXpAlUsuario → sube de nivel cuando se supera el umbral")
    void sumarXp_levelUp_whenThresholdReached() {
        gamificacionService.sumarXpAlUsuario(usuario.getId(), 100, null);

        Usuario actualizado = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(actualizado.getNivel()).isEqualTo(2);
    }

    @Test
    @DisplayName("sumarXpAlUsuario → registra una TransaccionXp en BD")
    void sumarXp_createsTransaccionXp() {
        gamificacionService.sumarXpAlUsuario(usuario.getId(), 75, null);

        var txs = transaccionXpRepository.findByUsuarioIdOrderByFechaGananciaDesc(usuario.getId());
        assertThat(txs).hasSize(1);
        assertThat(txs.get(0).getCantidadXp()).isEqualTo(75);
    }

    @Test
    @DisplayName("actualizarClasificacion → incrementa xpAcumulada en la temporada activa")
    void actualizarClasificacion_incrementsXpInActiveSeason() {
        // Requiere temporada activa y ClasificacionUsuario — los crea DataInitializer
        temporadaRepository.findByActivaTrue().ifPresent(temporada -> {
            Liga liga = ligaRepository.findAllByOrderByOrdenJerarquiaAsc().get(0);
            ClasificacionUsuarioKey key = new ClasificacionUsuarioKey(
                    usuario.getId(), liga.getId(), temporada.getId());
            clasificacionUsuarioRepository.save(ClasificacionUsuario.builder()
                    .id(key).usuario(usuario).liga(liga).temporada(temporada).xpAcumulada(0).build());

            gamificacionService.actualizarClasificacion(usuario.getId(), 200);

            ClasificacionUsuario cu = clasificacionUsuarioRepository.findById(key).orElseThrow();
            assertThat(cu.getXpAcumulada()).isEqualTo(200);
        });
    }

    // ── procesarCompletitudHabilidad ─────────────────────────────────────────

    @Test
    @DisplayName("procesarCompletitudHabilidad — idempotente si la habilidad ya estaba completada")
    void procesarCompletitud_alreadyComplete_doesNotDuplicate() {
        // Construir una habilidad con un ejercicio y marcarla completa manualmente
        Actividad act = actividadRepository.save(Actividad.builder().nombre("Act").build());
        Seccion sec = seccionRepository.save(
                Seccion.builder().actividad(act).orden(1).nombre("Sec").build());
        Habilidad hab = habilidadRepository.save(Habilidad.builder()
                .seccion(sec).orden(1).nombre("Hab")
                .dificultad(Dificultad.PRINCIPIANTE).build());
        Ejercicio ej = ejercicioRepository.save(Ejercicio.builder().nombre("Ej").build());
        habilidadEjercicioRepository.save(HabilidadEjercicio.builder()
                .id(new HabilidadEjercicioKey(hab.getId(), ej.getId()))
                .habilidad(hab).ejercicio(ej).orden(1).xpOtorgada(30).build());

        // Inicializar progreso raíz e indicar que el ejercicio de la habilidad está completado
        progresoHabilidadRepository.save(ProgresoHabilidad.builder()
                .id(new ProgresoHabilidadKey(usuario.getId(), hab.getId()))
                .usuario(usuario)
                .habilidad(hab)
                .completado(false)
                .build());

        progresoEjercicioRepository.save(ProgresoEjercicio.builder()
                .id(new ProgresoEjercicioKey(usuario.getId(), hab.getId(), ej.getId()))
                .usuario(usuario)
                .habilidad(hab)
                .ejercicio(ej)
                .completado(true)
                .build());

        // Primera llamada — completa la habilidad y suma la XP
        gamificacionService.procesarCompletitudHabilidad(usuario.getId(), hab.getId());
        // Segunda llamada — no debe crear transacciones duplicadas (idempotente)
        gamificacionService.procesarCompletitudHabilidad(usuario.getId(), hab.getId());

        var txs = transaccionXpRepository.findByUsuarioIdOrderByFechaGananciaDesc(usuario.getId());
        assertThat(txs).hasSize(1); // solo una transacción aunque se llamó dos veces
    }
}
