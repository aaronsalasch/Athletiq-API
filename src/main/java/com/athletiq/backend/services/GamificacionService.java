package com.athletiq.backend.services;

import java.util.UUID;

public interface GamificacionService {

    /**
     * Verifica si el usuario completó todos los ejercicios de una habilidad.
     * Si es así, marca ProgresoHabilidad como completado, registra la TransaccionXp
     * y suma los puntos al usuario y a su ClasificacionUsuario de la temporada activa.
     */
    void procesarCompletitudHabilidad(UUID usuarioId, UUID habilidadId);

    /**
     * Suma XP al usuario, registra la transacción y evalúa si sube de nivel.
     * También actualiza la xp_acumulada en ClasificacionUsuario.
     */
    void sumarXpAlUsuario(UUID usuarioId, int cantidad, UUID habilidadId);

    /**
     * Actualiza la xp_acumulada del usuario en la temporada activa.
     */
    void actualizarClasificacion(UUID usuarioId, int xpGanada);

    /**
     * Fórmula: nivel = floor(sqrt(puntosXp / 100)) + 1
     * Nivel 1: 0–99 XP | Nivel 2: 100–399 | Nivel 3: 400–899 | …
     */
    int calcularNivel(int puntosXp);
}
