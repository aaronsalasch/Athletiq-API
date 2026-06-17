package com.athletiq.backend.models.entities;

import com.athletiq.backend.models.enums.Dificultad;
import com.athletiq.backend.repositories.ActividadRepository;
import com.athletiq.backend.repositories.EjercicioRepository;
import com.athletiq.backend.repositories.HabilidadRepository;
import com.athletiq.backend.repositories.ImagenRepository;
import com.athletiq.backend.repositories.SeccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Imagen — Restricciones y Relaciones Polimórficas Exclusivas")
class ImagenConstraintTest {

    @Autowired private ImagenRepository imagenRepository;
    @Autowired private HabilidadRepository habilidadRepository;
    @Autowired private EjercicioRepository ejercicioRepository;
    @Autowired private ActividadRepository actividadRepository;
    @Autowired private SeccionRepository seccionRepository;

    private Habilidad habilidad;
    private Ejercicio ejercicio;
    private Actividad actividad;

    @BeforeEach
    void setUp() {
        actividad = actividadRepository.save(
                Actividad.builder().nombre("Calistenia").descripcion("Fuerza corporal").build());

        Seccion seccion = seccionRepository.save(Seccion.builder()
                .actividad(actividad).orden(1).nombre("Básicos").build());

        habilidad = habilidadRepository.save(Habilidad.builder()
                .seccion(seccion).orden(1).nombre("Flexiones")
                .dificultad(Dificultad.PRINCIPIANTE).tiempoEstimado(10).build());

        ejercicio = ejercicioRepository.save(
                Ejercicio.builder().nombre("Flexión estándar").descripcion("Flexión de pecho").build());
    }

    @Test
    @DisplayName("Guardar imagen asociada a Habilidad — Exito")
    void guardarImagen_habilidadSolo_success() {
        Imagen imagen = Imagen.builder()
                .urlImagen("http://example.com/hab.jpg")
                .nombreArchivo("hab.jpg")
                .descripcion("Demo habilidad")
                .habilidad(habilidad)
                .build();

        Imagen guardada = imagenRepository.save(imagen);
        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getFechaSubida()).isNotNull();
        assertThat(guardada.getHabilidad().getId()).isEqualTo(habilidad.getId());
    }

    @Test
    @DisplayName("Guardar imagen asociada a Ejercicio — Exito")
    void guardarImagen_ejercicioSolo_success() {
        Imagen imagen = Imagen.builder()
                .urlImagen("http://example.com/ejer.jpg")
                .nombreArchivo("ejer.jpg")
                .descripcion("Demo ejercicio")
                .ejercicio(ejercicio)
                .build();

        Imagen guardada = imagenRepository.save(imagen);
        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getEjercicio().getId()).isEqualTo(ejercicio.getId());
    }

    @Test
    @DisplayName("Guardar imagen asociada a Actividad — Exito")
    void guardarImagen_actividadSolo_success() {
        Imagen imagen = Imagen.builder()
                .urlImagen("http://example.com/act.jpg")
                .nombreArchivo("act.jpg")
                .descripcion("Demo actividad")
                .actividad(actividad)
                .build();

        Imagen guardada = imagenRepository.save(imagen);
        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getActividad().getId()).isEqualTo(actividad.getId());
    }

    @Test
    @DisplayName("Guardar imagen sin ninguna asociación — Falla con IllegalStateException")
    void guardarImagen_sinAsociacion_throwsException() {
        Imagen imagen = Imagen.builder()
                .urlImagen("http://example.com/fail.jpg")
                .nombreArchivo("fail.jpg")
                .descripcion("Sin asociación")
                .build();

        assertThatThrownBy(() -> imagenRepository.saveAndFlush(imagen))
                .hasMessageContaining("Una imagen debe estar asociada exactamente a una Habilidad, un Ejercicio o una Actividad");
    }

    @Test
    @DisplayName("Guardar imagen con múltiples asociaciones (Habilidad y Ejercicio) — Falla con IllegalStateException")
    void guardarImagen_multiAsociacion_throwsException() {
        Imagen imagen = Imagen.builder()
                .urlImagen("http://example.com/fail.jpg")
                .nombreArchivo("fail.jpg")
                .descripcion("Multiples asociaciones")
                .habilidad(habilidad)
                .ejercicio(ejercicio)
                .build();

        assertThatThrownBy(() -> imagenRepository.saveAndFlush(imagen))
                .hasMessageContaining("Una imagen debe estar asociada exactamente a una Habilidad, un Ejercicio o una Actividad");
    }
}
