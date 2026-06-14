package com.athletiq.backend.services.scheduled;

import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.repositories.*;
import com.athletiq.backend.services.EventoComunidadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Motor de Ligas — Cron Job.
 *
 * Se ejecuta cada domingo a medianoche (cron = "0 0 0 * * SUN").
 * Flujo:
 *   1. Cierra la Temporada activa.
 *   2. Lee todas las ClasificacionUsuario ordenadas por xp_acumulada DESC.
 *   3. Reasigna ligas por percentil (top X% → liga superior).
 *   4. Actualiza colorHexLiga en Usuario.
 *   5. Publica eventos de ascenso de liga (asíncrono).
 *   6. Crea la nueva Temporada.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LigaSchedulerService {

    private final TemporadaRepository temporadaRepository;
    private final ClasificacionUsuarioRepository clasificacionUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final LigaRepository ligaRepository;
    private final EventoComunidadService eventoComunidadService;

    @Scheduled(cron = "0 0 0 * * SUN")
    @Transactional
    public void procesarFinTemporada() {
        log.info("=== Inicio procesamiento fin de temporada ===");

        Temporada temporadaActual = temporadaRepository.findByActivaTrue().orElse(null);

        if (temporadaActual == null) {
            log.warn("No hay temporada activa. Creando la primera temporada.");
            crearNuevaTemporada();
            return;
        }

        // 1. Cerrar temporada
        temporadaActual.setActiva(false);
        temporadaActual.setFechaFin(LocalDate.now());
        temporadaRepository.save(temporadaActual);

        // 2. Obtener ranking completo de la temporada
        List<ClasificacionUsuario> ranking = clasificacionUsuarioRepository
                .findByTemporadaIdOrderByXpAcumuladaDesc(temporadaActual.getId());

        List<Liga> ligas = ligaRepository.findAllByOrderByOrdenJerarquiaAsc();

        if (!ranking.isEmpty() && !ligas.isEmpty()) {
            // 3. Reasignar ligas por percentil
            reasignarLigas(ranking, ligas);
        }

        // 4. Crear nueva temporada
        crearNuevaTemporada();

        log.info("=== Fin de temporada procesado. Nueva temporada iniciada. ===");
    }

    // ── Lógica de reasignación ────────────────────────────────────────────────

    /**
     * Distribución por cuantiles.
     *
     * Con N ligas y M usuarios:
     *   - top    (1/N) % → liga de mayor jerarquía
     *   - …
     *   - bottom (1/N) % → liga de menor jerarquía
     *
     * La lista ya viene ordenada DESC por xp_acumulada, por lo que
     * posición 0 = mejor jugador.
     */
    private void reasignarLigas(List<ClasificacionUsuario> ranking, List<Liga> ligas) {
        int total   = ranking.size();
        int numLigas = ligas.size();

        for (int i = 0; i < total; i++) {
            ClasificacionUsuario clasificacion = ranking.get(i);

            // Percentil: 1.0 → posición 0 (el mejor), 0.0 → último
            double percentil = 1.0 - ((double) i / total);
            // Índice en la lista ordenada ASC de ligas (0 = más baja, N-1 = más alta)
            int ligaIdx = Math.min((int) (percentil * numLigas), numLigas - 1);
            Liga nuevaLiga = ligas.get(ligaIdx);
            Liga ligaAnterior = clasificacion.getLiga();

            // Actualizar color de liga en el perfil del usuario
            Usuario usuario = clasificacion.getUsuario();
            usuario.setColorHexLiga(nuevaLiga.getColorHex());
            usuarioRepository.save(usuario);

            // Publicar ascenso si corresponde
            if (nuevaLiga.getOrdenJerarquia() > ligaAnterior.getOrdenJerarquia()) {
                eventoComunidadService.publishLigaAscenso(usuario.getId(), nuevaLiga.getId());
            }
        }
    }

    private void crearNuevaTemporada() {
        Temporada nueva = Temporada.builder()
                .fechaInicio(LocalDate.now())
                .activa(true)
                .build();
        temporadaRepository.save(nueva);
        log.info("Nueva temporada creada, inicio: {}", nueva.getFechaInicio());
    }
}
