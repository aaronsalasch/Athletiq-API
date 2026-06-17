package com.athletiq.backend.config;

import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.models.enums.Dificultad;
import com.athletiq.backend.models.keys.ClasificacionUsuarioKey;
import com.athletiq.backend.models.keys.HabilidadEjercicioKey;
import com.athletiq.backend.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.athletiq.backend.models.enums.TipoEvento;

/**
 * Siembra los datos mínimos necesarios para que la aplicación arranque correctamente:
 * roles, ligas, la primera temporada activa, datos de dominio y usuarios mock.
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
    private final ActividadRepository actividadRepository;
    private final SeccionRepository seccionRepository;
    private final HabilidadRepository habilidadRepository;
    private final EjercicioRepository ejercicioRepository;
    private final HabilidadEjercicioRepository habilidadEjercicioRepository;
    private final PasoRepository pasoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClasificacionUsuarioRepository clasificacionUsuarioRepository;
    private final EventoComunidadRepository eventoComunidadRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImagenRepository imagenRepository;

    @Bean
    @Transactional
    public CommandLineRunner seedData() {
        return args -> {
            seedRoles();
            seedLigas();
            Temporada temporadaActiva = seedTemporadaInicial();
            seedDomainData(temporadaActiva);
            seedImagesForExistingData();
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

    private Temporada seedTemporadaInicial() {
        Optional<Temporada> activaOpt = temporadaRepository.findByActivaTrue();
        if (activaOpt.isEmpty()) {
            Temporada temporada = Temporada.builder()
                    .fechaInicio(LocalDate.now())
                    .activa(true)
                    .build();
            Temporada saved = temporadaRepository.save(temporada);
            log.info("Primera temporada creada con inicio {}.", saved.getFechaInicio());
            return saved;
        }
        return activaOpt.get();
    }

    private Actividad saveActividad(String nombre, String descripcion) {
        return actividadRepository.save(Actividad.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .build());
    }

    private Seccion saveSeccion(Actividad actividad, int orden, String nombre, String descripcion) {
        return seccionRepository.save(Seccion.builder()
                .actividad(actividad)
                .orden(orden)
                .nombre(nombre)
                .descripcion(descripcion)
                .build());
    }

    private Habilidad saveHabilidad(Seccion seccion, int orden, String nombre, String descripcion, Dificultad dificultad, int tiempoEstimado) {
        return habilidadRepository.save(Habilidad.builder()
                .seccion(seccion)
                .orden(orden)
                .nombre(nombre)
                .descripcion(descripcion)
                .dificultad(dificultad)
                .tiempoEstimado(tiempoEstimado)
                .build());
    }

    private Ejercicio saveEjercicio(String nombre, String descripcion) {
        return ejercicioRepository.save(Ejercicio.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .build());
    }

    private void seedDomainData(Temporada temporada) {
        if (actividadRepository.count() > 0) {
            log.info("Los datos de dominio ya se encuentran sembrados.");
            seedUsuariosYClasificaciones(temporada);
            seedEventosComunidad();
            return;
        }

        log.info("Iniciando el sembrado de datos de dominio...");

        // =========================================================================
        // 1. ACTIVIDAD: PATINAJE
        // =========================================================================
        Actividad patinaje = saveActividad("Patinaje", "Desplazamiento, frenado y maniobras avanzadas sobre patines en línea.");

        // Secciones Patinaje
        Seccion desplPatinaje = saveSeccion(patinaje, 1, "Fundamentos de Desplazamiento", "Técnicas de balance, empuje y detención segura.");
        Seccion maniobrasPatinaje = saveSeccion(patinaje, 2, "Giros y Maniobras", "Control de trayectorias, crossovers y giros técnicos.");

        // --- DESPLAZAMIENTO ---
        Habilidad desplBasico = saveHabilidad(desplPatinaje, 1, "Desplazamiento Básico", "Balance y zancadas básicas en patines.", Dificultad.PRINCIPIANTE, 15);
        Habilidad frenosSeguros = saveHabilidad(desplPatinaje, 2, "Frenados de Seguridad", "Dominio de detenciones en diversas velocidades.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Desplazamiento
        Ejercicio posturaSeguridad = saveEjercicio("Postura de Seguridad (Ready Position)", "Postura de balance bajo para absorber impactos.");
        seedPasos(posturaSeguridad, List.of(
            new PasoSeed(1, "Posición", "Pies paralelos, rodillas dobladas hacia adelante tapando las puntas de los patines."),
            new PasoSeed(2, "Torso", "Mantén el torso ligeramente inclinado adelante, manos frente al cuerpo para estabilidad."),
            new PasoSeed(3, "Balance", "Sostén el peso centrado sobre las guías de los patines.")
        ));
        Ejercicio swizzle = saveEjercicio("Impulso de Limón (Lemon Swizzles)", "Desplazamiento básico sin levantar los patines del suelo.");
        seedPasos(swizzle, List.of(
            new PasoSeed(1, "Apertura", "Desde talones juntos, empuja los patines hacia afuera abriendo las piernas."),
            new PasoSeed(2, "Cierre", "Apunta los dedos hacia adentro usando aductores para juntar las puntas adelante."),
            new PasoSeed(3, "Inercia", "Repite el movimiento dibujando círculos/limones continuos en el suelo.")
        ));
        Ejercicio zancadaBasica = saveEjercicio("Zancada Básica (Basic Stride)", "Movimiento de patinaje estándar.");
        seedPasos(zancadaBasica, List.of(
            new PasoSeed(1, "Empuje", "Empuja lateralmente con un patín a 45 grados usando los cantos internos."),
            new PasoSeed(2, "Deslizamiento", "Desliza todo tu peso sobre el otro patín (patín de apoyo) manteniendo el balance."),
            new PasoSeed(3, "Recuperación", "Trae el patín de empuje de vuelta al centro y repite con el lado opuesto.")
        ));
        seedHabilidadEjercicio(desplBasico, posturaSeguridad, 1, 3, 10, 10);
        seedHabilidadEjercicio(desplBasico, swizzle, 2, 3, 12, 15);
        seedHabilidadEjercicio(desplBasico, zancadaBasica, 3, 4, 15, 25);

        // Ejercicios Frenos
        Ejercicio frenoTaco = saveEjercicio("Freno de Taco", "Detención utilizando el taco de goma del patín.");
        seedPasos(frenoTaco, List.of(
            new PasoSeed(1, "Tijera", "Adelanta el patín con freno (usualmente derecho) formando una tijera."),
            new PasoSeed(2, "Presión", "Eleva la punta del patín delantero para apoyar el taco de goma en el suelo."),
            new PasoSeed(3, "Control", "Baja el centro de gravedad flexionando la pierna trasera para frenar uniformemente.")
        ));
        Ejercicio frenoT = saveEjercicio("Freno en T (T-Stop)", "Frenado utilizando el patín trasero perpendicularmente.");
        seedPasos(frenoT, List.of(
            new PasoSeed(1, "Peso", "Transfiere todo el peso al patín de apoyo delantero."),
            new PasoSeed(2, "Arrastre", "Coloca el patín trasero perpendicular en forma de T raspando suavemente el suelo."),
            new PasoSeed(3, "Fricción", "Aplica presión progresiva sobre las 4 ruedas del patín de arrastre hasta detenerte.")
        ));
        seedHabilidadEjercicio(frenosSeguros, frenoTaco, 1, 3, 10, 15);
        seedHabilidadEjercicio(frenosSeguros, frenoT, 2, 4, 8, 35);

        // --- GIROS ---
        Habilidad girosBasicos = saveHabilidad(maniobrasPatinaje, 1, "Giros Básicos", "Cambios de dirección usando el peso del cuerpo.", Dificultad.PRINCIPIANTE, 15);
        Habilidad crossovers = saveHabilidad(maniobrasPatinaje, 2, "Cruces / Crossovers", "Cruzar un patín sobre el otro para acelerar en curvas.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Giros Básicos
        Ejercicio giroTijera = saveEjercicio("Giro en Tijera (Parallel Turn)", "Giro inclinando los patines de forma paralela.");
        seedPasos(giroTijera, List.of(
            new PasoSeed(1, "Tijera", "Coloca los patines en tijera (el patín interno del giro va adelantado)."),
            new PasoSeed(2, "Inclinación", "Inclina los tobillos y rodillas hacia la dirección interna del giro."),
            new PasoSeed(3, "Giro", "Deja que los cantos de las ruedas sigan la curva de forma paralela.")
        ));
        seedHabilidadEjercicio(girosBasicos, giroTijera, 1, 3, 10, 20);

        // Ejercicios Crossovers
        Ejercicio crossoverFwd = saveEjercicio("Crossover de Frente", "Paso cruzado frontal para mantener velocidad en curva.");
        seedPasos(crossoverFwd, List.of(
            new PasoSeed(1, "Inclinación", "Entra en la curva inclinando el cuerpo hacia el interior."),
            new PasoSeed(2, "Cruce", "Levanta el patín exterior y crúzalo completamente por encima del patín interior."),
            new PasoSeed(3, "Jalar", "Jala el patín que quedó abajo para devolverlo a la posición paralela.")
        ));
        seedHabilidadEjercicio(crossovers, crossoverFwd, 1, 3, 8, 35);


        // =========================================================================
        // 2. ACTIVIDAD: NATACIÓN
        // =========================================================================
        Actividad natacion = saveActividad("Natación", "Dominio del medio acuático, optimización de hidrodinámica y técnicas de estilos olímpicos.");

        // Secciones Natación
        Seccion freestyleNat = saveSeccion(natacion, 1, "Estilo Crol (Freestyle)", "Propulsión fluida, patada de tijera y respiración lateral.");
        Seccion breaststrokeNat = saveSeccion(natacion, 2, "Estilo Pecho (Breaststroke)", "Brazada simétrica bajo el agua, patada de rana y deslizamiento.");

        // --- CROL ---
        Habilidad crolPropulsion = saveHabilidad(freestyleNat, 1, "Propulsión de Crol", "Coordinación básica de patada y brazada.", Dificultad.PRINCIPIANTE, 15);
        Habilidad virajesSalidas = saveHabilidad(freestyleNat, 2, "Virajes y Salidas", "Entrada limpia al agua y giros continuos de pared.", Dificultad.INTERMEDIO, 25);

        // Ejercicios Propulsion Crol
        Ejercicio patadaCrolTabla = saveEjercicio("Patada de Crol con Tabla", "Trabajo aislado de propulsión de piernas.");
        seedPasos(patadaCrolTabla, List.of(
            new PasoSeed(1, "Sujeción", "Toma la tabla de natación adelante con brazos estirados."),
            new PasoSeed(2, "Patada", "Patea alternadamente desde la cadera, manteniendo tobillos relajados y rodillas ligeramente flexionadas."),
            new PasoSeed(3, "Respiración", "Mantén la cara en el agua y levántala al frente únicamente para respirar.")
        ));
        Ejercicio crolBilateral = saveEjercicio("Crol con Respiración Bilateral", "Estilo libre completo alternando lados de respiración.");
        seedPasos(crolBilateral, List.of(
            new PasoSeed(1, "Brazada", "Introduce la mano adelante alineada con el hombro, empuja el agua hacia atrás describiendo una S."),
            new PasoSeed(2, "Respiración", "Gira la cabeza hacia el lado lateral del brazo que recupera cada 3 brazadas, manteniendo una oreja sumergida."),
            new PasoSeed(3, "Patada", "Mantén una patada constante de 2 o 6 tiempos para balancear el cuerpo.")
        ));
        seedHabilidadEjercicio(crolPropulsion, patadaCrolTabla, 1, 3, 50, 15);
        seedHabilidadEjercicio(crolPropulsion, crolBilateral, 2, 4, 100, 35);

        // Ejercicios Virajes y Salidas
        Ejercicio flipTurn = saveEjercicio("Viraje de Campana (Flip Turn)", "Giro acrobático para rebotar en la pared.");
        seedPasos(flipTurn, List.of(
            new PasoSeed(1, "Aproximación", "Nada hacia la pared a máxima velocidad, guardando el último jalón de brazos."),
            new PasoSeed(2, "Giro", "Lleva la barbilla al pecho y encoge las rodillas haciendo una voltereta rápida en el agua."),
            new PasoSeed(3, "Impulso", "Coloca plantas de pies en la pared y empújate boca abajo en posición de flecha (streamline).")
        ));
        seedHabilidadEjercicio(virajesSalidas, flipTurn, 1, 3, 8, 30);

        // --- PECHO ---
        Habilidad pechoCoord = saveHabilidad(breaststrokeNat, 1, "Coordinación de Pecho", "Coordinación del ciclo de brazada, patada y planeo.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Pecho
        Ejercicio patadaRana = saveEjercicio("Patada de Rana (Breaststroke Kick)", "Patada simétrica de propulsión circular.");
        seedPasos(patadaRana, List.of(
            new PasoSeed(1, "Flexión", "Trae los talones hacia los glúteos doblando rodillas hacia afuera y flexionando tobillos."),
            new PasoSeed(2, "Empuje", "Patea hacia atrás en círculo empujando el agua con la planta de los pies."),
            new PasoSeed(3, "Cierre", "Junta las piernas con fuerza al final y desliza deslizando los pies juntos.")
        ));
        Ejercicio pechoCompleto = saveEjercicio("Pecho Completo", "Estilo de pecho sincronizado.");
        seedPasos(pechoCompleto, List.of(
            new PasoSeed(1, "Brazada", "Abre los brazos en corazón empujando hacia afuera y abajo, levantando la cabeza para respirar."),
            new PasoSeed(2, "Patada", "Lanza los brazos al frente en flecha a la vez que realizas la patada de rana."),
            new PasoSeed(3, "Deslizamiento", "Sostén la posición hidrodinámica 1 o 2 segundos antes de iniciar el siguiente ciclo.")
        ));
        seedHabilidadEjercicio(pechoCoord, patadaRana, 1, 3, 50, 15);
        seedHabilidadEjercicio(pechoCoord, pechoCompleto, 2, 4, 100, 40);


        // =========================================================================
        // 3. ACTIVIDAD: RUNNING
        // =========================================================================
        Actividad running = saveActividad("Running", "Eficiencia en carrera a pie, optimización de cadencia, resistencia y potencia aeróbica.");

        // Secciones Running
        Seccion tecnicRunning = saveSeccion(running, 1, "Técnica de Carrera", "Ejercicios de postura, zancada y apoyo del pie.");
        Seccion resistRunning = saveSeccion(running, 2, "Resistencia Aeróbica", "Desarrollo del sistema cardiovascular y umbral de lactato.");

        // --- TÉCNICA ---
        Habilidad posturaCadencia = saveHabilidad(tecnicRunning, 1, "Postura y Cadencia", "Mejora de la frecuencia de pasos y pisada.", Dificultad.PRINCIPIANTE, 15);
        Habilidad zancadaApoyo = saveHabilidad(tecnicRunning, 2, "Zancada y Apoyo", "Técnicas para correr con mayor impulso y evitar frenado.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Tecnica
        Ejercicio aSkips = saveEjercicio("Skipping A (A-Skips)", "Ejercicio de postura de rodilla alta y braceo.");
        seedPasos(aSkips, List.of(
            new PasoSeed(1, "Rodilla", "Eleva una rodilla a 90 grados manteniendo el pie flexionado hacia arriba (dorsiflexión)."),
            new PasoSeed(2, "Braceo", "Acompaña el movimiento con el brazo contrario a 90 grados."),
            new PasoSeed(3, "Ritmo", "Da un pequeño salto coordinando piernas de forma rítmica hacia adelante.")
        ));
        Ejercicio cadencia180 = saveEjercicio("Carrera a 180 ppm", "Entrenamiento de cadencia óptima para reducir impacto.");
        seedPasos(cadencia180, List.of(
            new PasoSeed(1, "Paso corto", "Acorta la zancada y aumenta la frecuencia de pasos."),
            new PasoSeed(2, "Apoyo bajo el cuerpo", "Asegúrate de apoyar el pie directamente debajo del centro de gravedad."),
            new PasoSeed(3, "Sincronía", "Usa un metrónomo o música a 180 bpm para sincronizar cada pisada.")
        ));
        seedHabilidadEjercicio(posturaCadencia, aSkips, 1, 3, 50, 10);
        seedHabilidadEjercicio(posturaCadencia, cadencia180, 2, 3, 10, 30);

        // Ejercicios Zancada
        Ejercicio apoyoMetatarso = saveEjercicio("Apoyo con Metatarso (Midfoot Strike)", "Evitar talonar en carrera.");
        seedPasos(apoyoMetatarso, List.of(
            new PasoSeed(1, "Postura", "Inclina ligeramente todo el cuerpo hacia adelante desde los tobillos."),
            new PasoSeed(2, "Contacto", "Aterriza con la parte media del pie, evitando golpear primero con el talón."),
            new PasoSeed(3, "Empuje", "Utiliza la flexión del tobillo y gemelos como un resorte natural.")
        ));
        seedHabilidadEjercicio(zancadaApoyo, apoyoMetatarso, 1, 3, 5, 25);

        // --- RESISTENCIA ---
        Habilidad zonaAerobica = saveHabilidad(resistRunning, 1, "Zona Aeróbica 2", "Entrenamiento de baja intensidad para resistencia base.", Dificultad.PRINCIPIANTE, 30);
        Habilidad intervalosFartlek = saveHabilidad(resistRunning, 2, "Intervalos y Fartlek", "Desarrollo de velocidad y VO2 máx.", Dificultad.AVANZADO, 25);

        // Ejercicios Resistencia
        Ejercicio zona2Run = saveEjercicio("Carrera de Zona 2", "Correr a un ritmo conversacional manteniendo pulso bajo.");
        seedPasos(zona2Run, List.of(
            new PasoSeed(1, "Frecuencia", "Mantén tus pulsaciones en la Zona 2 (60-70% de la FCM)."),
            new PasoSeed(2, "Ritmo constante", "Corre de manera constante pudiendo mantener una conversación completa sin jadear."),
            new PasoSeed(3, "Volumen", "Acumula tiempo bajo esta intensidad para mejorar la densidad mitocondrial.")
        ));
        Ejercicio fartlekSeries = saveEjercicio("Series Fartlek (Juego de Velocidad)", "Alternar ritmos rápidos y lentos de forma continua.");
        seedPasos(fartlekSeries, List.of(
            new PasoSeed(1, "Calentamiento", "Trota suave 10 minutos para calentar articulaciones."),
            new PasoSeed(2, "Intervalo rápido", "Realiza 1 minuto de carrera a ritmo fuerte de 5k (Zona 4/5)."),
            new PasoSeed(3, "Recuperación", "Trota 2 minutos suave para recuperar el aliento y repite 5-8 veces.")
        ));
        seedHabilidadEjercicio(zonaAerobica, zona2Run, 1, 1, 45, 30);
        seedHabilidadEjercicio(intervalosFartlek, fartlekSeries, 1, 1, 30, 45);


        // =========================================================================
        // 4. ACTIVIDAD: CALISTENIA
        // =========================================================================
        Actividad calistenia = saveActividad("Calistenia", "Entrenamiento físico centrado en el uso del propio peso corporal para fuerza y control.");

        // Secciones Calistenia
        Seccion pushCal = saveSeccion(calistenia, 1, "Fuerza de Empuje", "Ejercicios enfocados en empujar el propio peso corporal (pecho, hombros y tríceps).");
        Seccion pullCal = saveSeccion(calistenia, 2, "Fuerza de Tracción", "Ejercicios enfocados en jalar y traccionar el propio peso corporal (espalda y bíceps).");
        Seccion coreCal = saveSeccion(calistenia, 3, "Fuerza de Core", "Estabilidad del tronco, planchas y soportes abdominales avanzados.");
        Seccion legsCal = saveSeccion(calistenia, 4, "Fuerza de Piernas", "Fuerza y movilidad del tren inferior con peso corporal.");

        // --- PUSH ---
        Habilidad flexBasicas = saveHabilidad(pushCal, 1, "Flexiones Básicas", "Domina la técnica de flexiones sobre el suelo.", Dificultad.PRINCIPIANTE, 15);
        Habilidad fondosPecho = saveHabilidad(pushCal, 2, "Fondos de Pecho", "Aprende a realizar fondos en paralelas de forma estricta.", Dificultad.INTERMEDIO, 20);
        Habilidad flexParadaManos = saveHabilidad(pushCal, 3, "Flexiones en Parada de Manos", "Fuerza de empuje vertical y hombros en inversión.", Dificultad.AVANZADO, 25);

        // Ejercicios Flexiones Básicas
        Ejercicio flexInclinadas = saveEjercicio("Flexiones Inclinadas", "Flexión con manos elevadas para disminuir la resistencia.");
        seedPasos(flexInclinadas, List.of(
            new PasoSeed(1, "Posición", "Manos apoyadas en un banco alto o mesa, cuerpo recto."),
            new PasoSeed(2, "Descenso", "Baja el pecho doblando los codos controladamente."),
            new PasoSeed(3, "Empuje", "Presiona la superficie para volver a la posición inicial.")
        ));
        Ejercicio flexRodillas = saveEjercicio("Flexiones de Rodillas", "Flexión apoyando rodillas en el suelo.");
        seedPasos(flexRodillas, List.of(
            new PasoSeed(1, "Alineación", "Apoya rodillas y manos en el suelo. Espalda recta."),
            new PasoSeed(2, "Bajar", "Lleva el pecho hacia el suelo contrayendo el abdomen."),
            new PasoSeed(3, "Subir", "Empuja el suelo extendiendo los brazos.")
        ));
        Ejercicio flexRegulares = saveEjercicio("Flexiones Regulares", "Flexión estándar con pies y manos en el suelo.");
        seedPasos(flexRegulares, List.of(
            new PasoSeed(1, "Plancha alta", "Apoya pies y manos en el suelo. Todo el cuerpo activo."),
            new PasoSeed(2, "Bajar", "Dobla codos a 45 grados hasta rozar el suelo con el pecho."),
            new PasoSeed(3, "Subir", "Empuja con fuerza sin perder la alineación lumbar.")
        ));
        seedHabilidadEjercicio(flexBasicas, flexInclinadas, 1, 3, 12, 10);
        seedHabilidadEjercicio(flexBasicas, flexRodillas, 2, 3, 10, 15);
        seedHabilidadEjercicio(flexBasicas, flexRegulares, 3, 4, 8, 25);

        // Ejercicios Fondos
        Ejercicio fondosBanda = saveEjercicio("Fondos Asistidos con Banda", "Fondo en paralelas con banda elástica.");
        seedPasos(fondosBanda, List.of(
            new PasoSeed(1, "Ajuste", "Coloca la banda elástica cruzando las paralelas y pon las rodillas en ella."),
            new PasoSeed(2, "Descenso", "Baja flexionando codos a 90 grados."),
            new PasoSeed(3, "Empuje", "Presiona hacia arriba usando la resistencia de la banda.")
        ));
        Ejercicio fondosEstrictos = saveEjercicio("Fondos Estrictos", "Fondo estándar en barras paralelas.");
        seedPasos(fondosEstrictos, List.of(
            new PasoSeed(1, "Soporte", "Sostén tu peso con brazos estirados, hombros lejos de orejas."),
            new PasoSeed(2, "Descenso", "Inclina el torso y baja hasta que los brazos formen 90 grados."),
            new PasoSeed(3, "Retorno", "Presiona las paralelas para subir de forma explosiva.")
        ));
        Ejercicio fondosBarraRecta = saveEjercicio("Fondos en Barra Recta", "Fondo de pecho sobre barra horizontal simple.");
        seedPasos(fondosBarraRecta, List.of(
            new PasoSeed(1, "Apoyo", "Sostén el cuerpo arriba de la barra, apoyando la cadera superior."),
            new PasoSeed(2, "Bajar", "Lleva la barra hacia la boca del estómago doblando codos."),
            new PasoSeed(3, "Subida", "Empuja la barra con fuerza hasta bloquear brazos.")
        ));
        seedHabilidadEjercicio(fondosPecho, fondosBanda, 1, 3, 10, 15);
        seedHabilidadEjercicio(fondosPecho, fondosEstrictos, 2, 4, 8, 30);
        seedHabilidadEjercicio(fondosPecho, fondosBarraRecta, 3, 3, 6, 40);

        // Ejercicios Handstand Pushups
        Ejercicio flexPica = saveEjercicio("Flexiones de Pica en Suelo", "Flexión con cadera elevada simulando empuje vertical.");
        seedPasos(flexPica, List.of(
            new PasoSeed(1, "Postura", "En posición de V invertida, manos y pies en el suelo, mirada al ombligo."),
            new PasoSeed(2, "Descenso", "Lleva la coronilla por delante de las manos formando un trípode."),
            new PasoSeed(3, "Empujar", "Empuja hacia atrás y arriba para regresar a la V invertida.")
        ));
        Ejercicio flexPicaElevadas = saveEjercicio("Flexiones de Pica Elevadas", "Flexiones de pica con los pies sobre un banco.");
        seedPasos(flexPicaElevadas, List.of(
            new PasoSeed(1, "Apoyo", "Coloca los pies sobre un banco y camina con las manos hacia el banco."),
            new PasoSeed(2, "Trípode", "Baja la cabeza de forma controlada dibujando un triángulo con las manos."),
            new PasoSeed(3, "Bloqueo", "Empuja extendiendo hombros y brazos por completo.")
        ));
        Ejercicio hspdPared = saveEjercicio("Flexiones HSPU en Pared", "Flexiones verticales apoyando la espalda en la pared.");
        seedPasos(hspdPared, List.of(
            new PasoSeed(1, "Inversión", "Patea y sube a parada de manos con talones en la pared."),
            new PasoSeed(2, "Flexión", "Baja la cabeza controladamente hasta rozar el suelo."),
            new PasoSeed(3, "Empuje Máximo", "Empuja con potencia los hombros para bloquear brazos arriba.")
        ));
        seedHabilidadEjercicio(flexParadaManos, flexPica, 1, 3, 10, 20);
        seedHabilidadEjercicio(flexParadaManos, flexPicaElevadas, 2, 3, 8, 30);
        seedHabilidadEjercicio(flexParadaManos, hspdPared, 3, 4, 4, 50);

        // --- PULL ---
        Habilidad remosCorporales = saveHabilidad(pullCal, 1, "Remo Corporal / Inverted Rows", "Fuerza básica de tracción horizontal.", Dificultad.PRINCIPIANTE, 15);
        Habilidad dominadasBasicas = saveHabilidad(pullCal, 2, "Dominadas Básicas", "Domina tu primer jalón estricto en barra.", Dificultad.INTERMEDIO, 20);
        Habilidad muscleUp = saveHabilidad(pullCal, 3, "Muscle-Up de Barra", "Tracción explosiva y transición sobre la barra.", Dificultad.AVANZADO, 30);

        // Ejercicios Remos
        Ejercicio remosInclinados = saveEjercicio("Remos Inclinados", "Tracción horizontal con barra a la altura del pecho.");
        seedPasos(remosInclinados, List.of(
            new PasoSeed(1, "Agarre", "Sujeta la barra baja, cuerpo inclinado a 45 grados."),
            new PasoSeed(2, "Jalar", "Lleva el pecho hacia la barra contrayendo la espalda."),
            new PasoSeed(3, "Extender", "Vuelve con control estirando los brazos.")
        ));
        Ejercicio remosAustralianos = saveEjercicio("Remos Australianos en Paralelas", "Remo horizontal usando barras paralelas.");
        seedPasos(remosAustralianos, List.of(
            new PasoSeed(1, "Cuerpo recto", "Cuelga debajo de las paralelas con talones apoyados en el suelo."),
            new PasoSeed(2, "Tracción", "Jala llevando los hombros hacia las barras paralelas."),
            new PasoSeed(3, "Control", "Baja despacio manteniendo la cadera alta y alineada.")
        ));
        seedHabilidadEjercicio(remosCorporales, remosInclinados, 1, 3, 12, 10);
        seedHabilidadEjercicio(remosCorporales, remosAustralianos, 2, 4, 10, 20);

        // Ejercicios Dominadas
        Ejercicio domEscapulares = saveEjercicio("Dominadas Escapulares", "Activación escapular sin doblar codos.");
        seedPasos(domEscapulares, List.of(
            new PasoSeed(1, "Agarre", "Cuélgate de la barra con agarre prono."),
            new PasoSeed(2, "Activación", "Retrae las escápulas subiendo los hombros con los brazos rectos."),
            new PasoSeed(3, "Relajar", "Sostén un segundo y relaja escapularmente.")
        ));
        Ejercicio domNegativas = saveEjercicio("Dominadas Negativas", "Fase excéntrica controlada partiendo de arriba.");
        seedPasos(domNegativas, List.of(
            new PasoSeed(1, "Subida asistida", "Usa un banco o salta para quedar con la barbilla sobre la barra."),
            new PasoSeed(2, "Descenso lento", "Baja lo más lento posible (4 a 5 segundos) controlando el peso."),
            new PasoSeed(3, "Estiramiento", "Llega al bloqueo completo de brazos abajo.")
        ));
        Ejercicio domEstrictas = saveEjercicio("Dominadas Estrictas", "Dominada estándar con rango completo.");
        seedPasos(domEstrictas, List.of(
            new PasoSeed(1, "Cuelgue activo", "Brazos estirados, escápulas activas."),
            new PasoSeed(2, "Tracción", "Jala con los codos hacia abajo llevando el pecho a la barra."),
            new PasoSeed(3, "Descenso", "Baja lentamente hasta la posición inicial de cuelgue.")
        ));
        seedHabilidadEjercicio(dominadasBasicas, domEscapulares, 1, 3, 10, 10);
        seedHabilidadEjercicio(dominadasBasicas, domNegativas, 2, 3, 6, 20);
        seedHabilidadEjercicio(dominadasBasicas, domEstrictas, 3, 4, 5, 35);

        // Ejercicios Muscle-Up
        Ejercicio domExplosivas = saveEjercicio("Dominadas Explosivas al Pecho", "Dominada rápida buscando máxima altura.");
        seedPasos(domExplosivas, List.of(
            new PasoSeed(1, "Arranque", "Tracciona de forma explosiva llevando la barra al ombligo o esternón."),
            new PasoSeed(2, "Inercia", "Empuja los codos hacia atrás rápidamente en el punto más alto.")
        ));
        Ejercicio transicionMu = saveEjercicio("Transición de Muscle-Up Asistida", "Práctica de la rotación de muñecas y codos.");
        seedPasos(transicionMu, List.of(
            new PasoSeed(1, "Agarre falso", "Usa agarre falso (false grip) en barra o anillas."),
            new PasoSeed(2, "Rotación", "Pasa el pecho por encima de la barra rotando codos velozmente.")
        ));
        Ejercicio muscleUpEstricto = saveEjercicio("Muscle-Up Estricto", "Subida fluida y de fuerza sobre la barra.");
        seedPasos(muscleUpEstricto, List.of(
            new PasoSeed(1, "Jalón alto", "Tira con fuerza hacia abajo y atrás superando la barra con el pecho."),
            new PasoSeed(2, "Rotación y empuje", "Gira los codos y realiza un fondo de barra para bloquear arriba.")
        ));
        seedHabilidadEjercicio(muscleUp, domExplosivas, 1, 3, 8, 25);
        seedHabilidadEjercicio(muscleUp, transicionMu, 2, 3, 5, 35);
        seedHabilidadEjercicio(muscleUp, muscleUpEstricto, 3, 3, 3, 60);

        // --- CORE ---
        Habilidad planchaAbs = saveHabilidad(coreCal, 1, "Plancha Abdominal", "Estabilidad central y postura de core básica.", Dificultad.PRINCIPIANTE, 15);
        Habilidad lSit = saveHabilidad(coreCal, 2, "L-Sit en Paralelas", "Soporte escapular y flexión isométrica de cadera.", Dificultad.INTERMEDIO, 20);
        Habilidad frontLever = saveHabilidad(coreCal, 3, "Front Lever Pro", "Isometría de tracción horizontal y estabilidad abdominal.", Dificultad.AVANZADO, 30);
        // Ejercicios Plancha Abdominal
        Ejercicio planchaAntebrazos = saveEjercicio("Plancha de Antebrazos", "Estabilidad central isométrica sobre los antebrazos.");
        seedPasos(planchaAntebrazos, List.of(
            new PasoSeed(1, "Posición", "Apoya los antebrazos en el suelo, con los codos alineados justo debajo de los hombros."),
            new PasoSeed(2, "Alineación", "Extiende las piernas apoyando las puntas de los pies, formando una línea recta desde la cabeza hasta los talones."),
            new PasoSeed(3, "Activación", "Contrae abdomen y glúteos, empujando activamente el suelo para mantener las escápulas activas.")
        ));
        Ejercicio planchaAlta = saveEjercicio("Plancha Alta (High Plank)", "Soporte de plancha con los brazos extendidos.");
        seedPasos(planchaAlta, List.of(
            new PasoSeed(1, "Apoyo", "Coloca las manos en el suelo directamente debajo de los hombros, con los brazos completamente estirados."),
            new PasoSeed(2, "Estabilidad", "Empuja los talones hacia atrás y activa cuadríceps, abdomen y glúteos."),
            new PasoSeed(3, "Postura", "Mantén el cuello neutro y una línea recta en la columna sin arquear la zona lumbar.")
        ));
        seedHabilidadEjercicio(planchaAbs, planchaAntebrazos, 1, 3, 30, 15);
        seedHabilidadEjercicio(planchaAbs, planchaAlta, 2, 3, 45, 20);

        // Ejercicios L-Sit
        Ejercicio rodillasColgado = saveEjercicio("Levantamiento de Rodillas Colgado", "Activación del core colgado de la barra.");
        seedPasos(rodillasColgado, List.of(
            new PasoSeed(1, "Cuelgue", "Brazos estirados, torso firme."),
            new PasoSeed(2, "Elevación", "Sube las rodillas dobladas hacia el pecho sin balancearte."),
            new PasoSeed(3, "Bajar", "Baja las piernas de manera controlada.")
        ));
        Ejercicio soporteLSit = saveEjercicio("Soporte L-Sit en Paralelas", "Isometría en paralelas con piernas estiradas.");
        seedPasos(soporteLSit, List.of(
            new PasoSeed(1, "Soporte", "Sostente en paralelas con brazos rectos, hombros deprimidos."),
            new PasoSeed(2, "Extensión", "Eleva y estira las piernas a 90 grados respecto al torso."),
            new PasoSeed(3, "Mantener", "Sostén la posición bloqueando rodillas todo el tiempo indicado.")
        ));
        seedHabilidadEjercicio(lSit, rodillasColgado, 1, 3, 12, 15);
        seedHabilidadEjercicio(lSit, soporteLSit, 2, 3, 15, 30);

        // Ejercicios Front Lever
        Ejercicio tuckFrontLever = saveEjercicio("Tuck Front Lever Hold", "Isometría de palanca frontal con rodillas encogidas.");
        seedPasos(tuckFrontLever, List.of(
            new PasoSeed(1, "Tracción", "Jala la barra manteniendo los brazos completamente estirados."),
            new PasoSeed(2, "Encogimiento", "Eleva la espalda dejándola paralela al suelo, encogiendo rodillas al pecho."),
            new PasoSeed(3, "Alineación", "Mantén la cadera y hombros en la misma línea horizontal.")
        ));
        Ejercicio frontLeverCompleto = saveEjercicio("Front Lever Completo", "Posición isométrica horizontal completa.");
        seedPasos(frontLeverCompleto, List.of(
            new PasoSeed(1, "Palanca", "Jala con brazos bloqueados manteniendo el cuerpo completamente extendido."),
            new PasoSeed(2, "Alineación horizontal", "Sostén el cuerpo entero en paralelo al suelo como una tabla.")
        ));
        seedHabilidadEjercicio(frontLever, tuckFrontLever, 1, 4, 10, 30);
        seedHabilidadEjercicio(frontLever, frontLeverCompleto, 2, 4, 5, 70);

        // --- LEGS ---
        Habilidad sentadillasLibres = saveHabilidad(legsCal, 1, "Sentadillas Libres", "Fuerza básica y rango de movimiento de tren inferior.", Dificultad.PRINCIPIANTE, 15);
        Habilidad pistolSquat = saveHabilidad(legsCal, 2, "Sentadilla de Pistola / Pistol Squat", "Fuerza unilateral y balance avanzado.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Sentadillas Libres
        Ejercicio sentadillasBasicasEj = saveEjercicio("Sentadillas Libres", "Sentadilla tradicional con peso corporal.");
        seedPasos(sentadillasBasicasEj, List.of(
            new PasoSeed(1, "Postura", "Pies al ancho de hombros, puntas ligeramente hacia afuera."),
            new PasoSeed(2, "Descenso", "Baja la cadera hacia atrás y abajo manteniendo la espalda recta."),
            new PasoSeed(3, "Empuje", "Presiona con fuerza los talones para ponerte de pie.")
        ));
        seedHabilidadEjercicio(sentadillasLibres, sentadillasBasicasEj, 1, 3, 15, 15);

        // Ejercicios Pistol Squat
        Ejercicio bulgara = saveEjercicio("Sentadilla Búlgara", "Sentadilla unilateral apoyando un pie atrás.");
        seedPasos(bulgara, List.of(
            new PasoSeed(1, "Apoyo", "Apoya un pie atrás sobre un banco y el otro al frente en el suelo."),
            new PasoSeed(2, "Bajar", "Desciende la cadera de forma vertical hasta que la rodilla trasera roce el suelo."),
            new PasoSeed(3, "Fuerza", "Empuja con la pierna delantera manteniendo el torso erguido.")
        ));
        Ejercicio pistolSquatEstricto = saveEjercicio("Pistol Squat Estricto", "Sentadilla completa con una sola pierna libre.");
        seedPasos(pistolSquatEstricto, List.of(
            new PasoSeed(1, "Balance", "Párate en un pie, extiende la pierna libre y brazos hacia adelante."),
            new PasoSeed(2, "Profundidad", "Baja de forma controlada hasta que el muslo toque la pantorrilla."),
            new PasoSeed(3, "Subida", "Empuja con el talón de la pierna de apoyo manteniendo el equilibrio.")
        ));
        seedHabilidadEjercicio(pistolSquat, bulgara, 1, 3, 10, 15);
        seedHabilidadEjercicio(pistolSquat, pistolSquatEstricto, 2, 3, 6, 40);


        // =========================================================================
        // 2. ACTIVIDAD: YOGA Y MOVILIDAD
        // =========================================================================
        Actividad yoga = saveActividad("Yoga y Movilidad", "Apertura articular, balances en brazos y flexibilidad general con un enfoque integral.");

        // Secciones Yoga
        Seccion hipYoga = saveSeccion(yoga, 1, "Flexibilidad de Cadera", "Apertura y estiramiento profundo de flexores de cadera y glúteos.");
        Seccion armsYoga = saveSeccion(yoga, 2, "Balances de Brazos", "Fuerza en muñecas, hombros y control del centro de gravedad.");
        Seccion backYoga = saveSeccion(yoga, 3, "Extensiones de Columna", "Apertura de pecho y flexibilidad de la columna vertebral.");

        // --- CADERA ---
        Habilidad aperturaLateral = saveHabilidad(hipYoga, 1, "Apertura Lateral / Mariposa", "Movilidad y estiramiento de aductores.", Dificultad.PRINCIPIANTE, 15);
        Habilidad aperturaLongitudinal = saveHabilidad(hipYoga, 2, "Apertura Longitudinal / Splits", "Estiramiento profundo de isquiotibiales y psoas.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Mariposa
        Ejercicio mariposaPostura = saveEjercicio("Postura de la Mariposa (Baddha Konasana)", "Estiramiento sentado con plantas de pies juntas.");
        seedPasos(mariposaPostura, List.of(
            new PasoSeed(1, "Sentado", "Junta las plantas de los pies trayendo los talones lo más cerca posible del pubis."),
            new PasoSeed(2, "Apertura", "Abre las rodillas hacia los lados intentando acercarlas al suelo con la espalda erguida."),
            new PasoSeed(3, "Inclinación", "Lleva el pecho hacia adelante manteniendo la flexión de cadera.")
        ));
        seedHabilidadEjercicio(aperturaLateral, mariposaPostura, 1, 3, 30, 15);

        // Ejercicios Splits
        Ejercicio palomaPostura = saveEjercicio("Postura de la Paloma (Kapotasana)", "Apertura profunda de cadera unilateral.");
        seedPasos(palomaPostura, List.of(
            new PasoSeed(1, "Alineación", "Trae una rodilla adelante detrás de la muñeca del mismo lado, extendiendo la pierna trasera atrás."),
            new PasoSeed(2, "Apoyo", "Alinea la pelvis y baja los antebrazos al suelo extendiendo la espalda."),
            new PasoSeed(3, "Respiración", "Mantén la postura respirando profundamente para relajar el glúteo.")
        ));
        Ejercicio splitCompleto = saveEjercicio("Split Longitudinal Completo (Hanumanasana)", "Apertura completa de piernas adelante y atrás.");
        seedPasos(splitCompleto, List.of(
            new PasoSeed(1, "Deslizamiento", "Apoya bloques a los lados y desliza el talón delantero y la rodilla trasera en direcciones opuestas."),
            new PasoSeed(2, "Estiramiento", "Mantén la cadera cuadrada al frente y baja los glúteos al suelo."),
            new PasoSeed(3, "Postura", "Eleva el torso y extiende los brazos hacia arriba manteniendo el balance.")
        ));
        seedHabilidadEjercicio(aperturaLongitudinal, palomaPostura, 1, 3, 30, 20);
        seedHabilidadEjercicio(aperturaLongitudinal, splitCompleto, 2, 3, 20, 50);

        // --- BALANCES ---
        Habilidad bakasana = saveHabilidad(armsYoga, 1, "Postura del Cuervo / Bakasana", "Tu primer balance sobre las manos coordinando core y balance.", Dificultad.PRINCIPIANTE, 15);
        Habilidad pinchaMayurasana = saveHabilidad(armsYoga, 2, "Balance en Antebrazos / Pincha", "Inversión vertical controlada sobre los antebrazos.", Dificultad.INTERMEDIO, 25);

        // Ejercicios Bakasana
        Ejercicio cuervoAsistido = saveEjercicio("Soporte de Cuervo con Bloques", "Fase preparatoria elevando un pie a la vez.");
        seedPasos(cuervoAsistido, List.of(
            new PasoSeed(1, "Posición", "Coloca las manos firmes en el suelo al ancho de hombros."),
            new PasoSeed(2, "Rodillas", "Apoya las rodillas altas en la parte trasera de los tríceps."),
            new PasoSeed(3, "Inclinación", "Inclina el peso adelante despegando un pie y luego el otro con asistencia.")
        ));
        Ejercicio cuervoCompleto = saveEjercicio("Postura del Cuervo (Bakasana)", "Balance completo sobre manos.");
        seedPasos(cuervoCompleto, List.of(
            new PasoSeed(1, "Mirada", "Fija la mirada un poco adelante del suelo para equilibrar."),
            new PasoSeed(2, "Core", "Contrae el abdomen, eleva los pies del suelo y junta los dedos gordos."),
            new PasoSeed(3, "Soporte", "Mantén los codos estirados en lo posible con rodillas presionando los brazos.")
        ));
        seedHabilidadEjercicio(bakasana, cuervoAsistido, 1, 3, 10, 15);
        seedHabilidadEjercicio(bakasana, cuervoCompleto, 2, 3, 15, 35);

        // Ejercicios Pincha
        Ejercicio delfin = saveEjercicio("Postura del Delfín", "Fortalecimiento de hombros y core en antebrazos.");
        seedPasos(delfin, List.of(
            new PasoSeed(1, "Postura", "Apoya antebrazos paralelos en el suelo y camina con los pies hacia los codos."),
            new PasoSeed(2, "Elevación", "Empuja activamente el suelo con los hombros elevando la cadera.")
        ));
        Ejercicio pinchaCompleta = saveEjercicio("Pincha Mayurasana Libre", "Soporte vertical invertido completo en antebrazos.");
        seedPasos(pinchaCompleta, List.of(
            new PasoSeed(1, "Patear", "Eleva una pierna y da un salto suave para alinear la cadera sobre los codos."),
            new PasoSeed(2, "Alineación", "Junta las piernas, apunta con los pies al techo y arquea mínimamente la espalda."),
            new PasoSeed(3, "Balance", "Ajusta el centro de gravedad con los codos y muñecas.")
        ));
        seedHabilidadEjercicio(pinchaMayurasana, delfin, 1, 3, 20, 20);
        seedHabilidadEjercicio(pinchaMayurasana, pinchaCompleta, 2, 4, 10, 45);

        // --- ESPALDA ---
        Habilidad dhanurasana = saveHabilidad(backYoga, 1, "Postura del Arco / Dhanurasana", "Extensión prona de columna y apertura pectoral.", Dificultad.PRINCIPIANTE, 15);
        Habilidad ruedaChakrasana = saveHabilidad(backYoga, 2, "Postura de la Rueda / Chakrasana", "Flexión hacia atrás e inversión de hombros completa.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Arco
        Ejercicio cobraPostura = saveEjercicio("Postura de la Cobra (Bhujangasana)", "Extensión lumbar apoyando las manos en el suelo.");
        seedPasos(cobraPostura, List.of(
            new PasoSeed(1, "Prono", "Acuéstate boca abajo con las manos a los lados del pecho."),
            new PasoSeed(2, "Extensión", "Presiona el pubis contra el suelo y levanta el pecho estirando brazos a la mitad."),
            new PasoSeed(3, "Hombros", "Rota los hombros hacia atrás abriendo el pectoral superior.")
        ));
        Ejercicio arcoPostura = saveEjercicio("Postura del Arco (Dhanurasana)", "Arco completo sujetando los tobillos con las manos.");
        seedPasos(arcoPostura, List.of(
            new PasoSeed(1, "Sujeción", "Boca abajo, dobla rodillas y toma los tobillos por fuera."),
            new PasoSeed(2, "Elevación", "Patea con los pies hacia atrás y arriba, levantando muslos y pecho del suelo."),
            new PasoSeed(3, "Mirada", "Sostén la mirada adelante abriendo la caja torácica.")
        ));
        seedHabilidadEjercicio(dhanurasana, cobraPostura, 1, 3, 20, 15);
        seedHabilidadEjercicio(dhanurasana, arcoPostura, 2, 3, 15, 30);

        // Ejercicios Rueda
        Ejercicio ruedaCompleta = saveEjercicio("Postura de la Rueda (Urdhva Dhanurasana)", "Arco de espalda completo.");
        seedPasos(ruedaCompleta, List.of(
            new PasoSeed(1, "Preparación", "Boca arriba, dobla rodillas apoyando pies cerca de glúteos. Manos al lado de las orejas."),
            new PasoSeed(2, "Empuje", "Presiona pies y manos para levantar la cadera y coronilla al suelo."),
            new PasoSeed(3, "Bloqueo", "Estira codos e intenta llevar el pecho hacia adelante abriendo hombros.")
        ));
        seedHabilidadEjercicio(ruedaChakrasana, ruedaCompleta, 1, 3, 15, 45);


        // =========================================================================
        // 3. ACTIVIDAD: GIMNASIA ARTÍSTICA
        // =========================================================================
        Actividad gimnasia = saveActividad("Gimnasia Artística", "Fuerza isométrica extrema en anillas, control postural y dominio de parada de manos.");

        // Secciones Gimnasia
        Seccion ringsGym = saveSeccion(gimnasia, 1, "Fuerza en Anillas", "Estabilización y fuerza de empuje sobre suspensión inestable.");
        Seccion handstandGym = saveSeccion(gimnasia, 2, "Parada de Manos", "Control corporal invertido en la disciplina gimnástica.");

        // --- ANILLAS ---
        Habilidad soporteAnillas = saveHabilidad(ringsGym, 1, "Soporte de Anillas", "Estabilización estricta por encima del plano de apoyo.", Dificultad.PRINCIPIANTE, 15);
        Habilidad fondosAnillas = saveHabilidad(ringsGym, 2, "Fondos en Anillas", "Fuerza de empuje dinámica con inestabilidad tridimensional.", Dificultad.INTERMEDIO, 20);

        // Ejercicios Soporte
        Ejercicio soporteHold = saveEjercicio("Soporte Activo de Anillas (Rings Hold)", "Sostén estático manteniendo las anillas pegadas al cuerpo.");
        seedPasos(soporteHold, List.of(
            new PasoSeed(1, "Salto", "Sube a las anillas con brazos estirados y deprime hombros."),
            new PasoSeed(2, "Alineación", "Mantén el cuerpo recto y presiona las anillas firmes contra la cadera."),
            new PasoSeed(3, "Turnout", "Rota externamente las manos de forma que las palmas miren al frente.")
        ));
        seedHabilidadEjercicio(soporteAnillas, soporteHold, 1, 3, 15, 20);

        // Ejercicios Fondos
        Ejercicio fondosAnillasEj = saveEjercicio("Fondos en Anillas Estrictos", "Fondo dinámico estabilizando el temblor muscular.");
        seedPasos(fondosAnillasEj, List.of(
            new PasoSeed(1, "Inicio", "Comienza en la posición de soporte activo arriba."),
            new PasoSeed(2, "Descenso", "Baja despacio flexionando codos a 90 grados, controlando la separación de las anillas."),
            new PasoSeed(3, "Empuje", "Presiona hacia arriba bloqueando codos y finalizando en turnout.")
        ));
        seedHabilidadEjercicio(fondosAnillas, fondosAnillasEj, 1, 3, 8, 40);

        // --- HANDSTAND ---
        Habilidad paredHandstand = saveHabilidad(handstandGym, 1, "Parada de Manos en Pared", "Ganancia de fuerza y alineación escapular.", Dificultad.PRINCIPIANTE, 15);
        Habilidad libreHandstand = saveHabilidad(handstandGym, 2, "Parada de Manos Libre", "Equilibrio puro controlando el peso con los dedos y muñecas.", Dificultad.INTERMEDIO, 25);

        // Ejercicios Pared
        Ejercicio wallWalk = saveEjercicio("Caminata de Pared (Wall Walk)", "Subida controlada de espaldas hasta quedar vertical.");
        seedPasos(wallWalk, List.of(
            new PasoSeed(1, "Plancha", "Empieza en posición de flexión con los pies tocando la pared."),
            new PasoSeed(2, "Caminata", "Camina con los pies hacia arriba por la pared a la vez que desplazas las manos hacia atrás."),
            new PasoSeed(3, "Alineación", "Queda con el pecho pegado a la pared, empujando activamente el suelo con hombros.")
        ));
        seedHabilidadEjercicio(paredHandstand, wallWalk, 1, 3, 30, 15);

        // Ejercicios Libre
        Ejercicio kickUp = saveEjercicio("Lanzamientos de Kick-Up", "Pateo controlado para entrar en la vertical.");
        seedPasos(kickUp, List.of(
            new PasoSeed(1, "Estocada", "Da un paso adelante extendiendo brazos sobre las orejas."),
            new PasoSeed(2, "Lanzamiento", "Apoya manos en el suelo y patea con la pierna trasera elevando la cadera."),
            new PasoSeed(3, "Alineación", "Junta las piernas en el aire buscando el eje vertical.")
        ));
        Ejercicio hsLibre = saveEjercicio("Parada de Manos Libre", "Soporte estático sin apoyos.");
        seedPasos(hsLibre, List.of(
            new PasoSeed(1, "Vertical", "Entra en parada de manos mediante kick-up o press."),
            new PasoSeed(2, "Foco", "Empuja el suelo con hombros (elevación escapular) y contrae abdomen."),
            new PasoSeed(3, "Dedos", "Usa la yema de tus dedos (garra) para corregir el balance si te vas al frente.")
        ));
        seedHabilidadEjercicio(libreHandstand, kickUp, 1, 3, 10, 15);
        seedHabilidadEjercicio(libreHandstand, hsLibre, 2, 4, 10, 50);


        // =========================================================================
        // 4. ACTIVIDAD: HALTEROFILIA
        // =========================================================================
        Actividad halterofilia = saveActividad("Halterofilia", "Levantamiento de peso olímpico enfocado en potencia, técnica y velocidad.");

        // Secciones Halterofilia
        Seccion snatchHalt = saveSeccion(halterofilia, 1, "Arranque (Snatch)", "Levantamiento de la barra en un solo movimiento fluido sobre la cabeza.");
        Seccion cleanJerkHalt = saveSeccion(halterofilia, 2, "Envión (Clean & Jerk)", "Levantamiento en dos tiempos: cargada al pecho y empuje vertical.");

        // --- SNATCH ---
        Habilidad powerSnatch = saveHabilidad(snatchHalt, 1, "Arranque de Fuerza / Power Snatch", "Levantamiento de barra sin flexión profunda de rodillas.", Dificultad.INTERMEDIO, 20);
        Habilidad squatSnatch = saveHabilidad(snatchHalt, 2, "Arranque Olímpico / Squat Snatch", "Movimiento oficial con caída profunda debajo de la barra.", Dificultad.AVANZADO, 35);

        // Ejercicios Power Snatch
        Ejercicio snatchPull = saveEjercicio("Jalón Alto de Arranque (Snatch High Pull)", "Tracción explosiva para aprender la extensión triple.");
        seedPasos(snatchPull, List.of(
            new PasoSeed(1, "Tirón", "Sujeta la barra con agarre ancho (snatch grip) y sube extendiendo cadera."),
            new PasoSeed(2, "Extensión", "Sube hombros de forma explosiva llevando la barra lo más alta posible pegada al cuerpo.")
        ));
        Ejercicio powerSnatchEj = saveEjercicio("Power Snatch con Barra", "Arranque de fuerza completo.");
        seedPasos(powerSnatchEj, List.of(
            new PasoSeed(1, "Salida", "Espalda recta, hombros por delante de la barra."),
            new PasoSeed(2, "Extensión triple", "Extiende tobillos, rodillas y cadera explosivamente."),
            new PasoSeed(3, "Recepción", "Deslízate ligeramente abajo y recibe la barra con brazos bloqueados arriba.")
        ));
        seedHabilidadEjercicio(powerSnatch, snatchPull, 1, 3, 5, 20);
        seedHabilidadEjercicio(powerSnatch, powerSnatchEj, 2, 3, 3, 40);

        // Ejercicios Squat Snatch
        Ejercicio snatchBalance = saveEjercicio("Balanza de Arranque (Snatch Balance)", "Desarrollo de velocidad de caída bajo la barra.");
        seedPasos(snatchBalance, List.of(
            new PasoSeed(1, "Soporte", "Barra apoyada en trapecios superiores, manos en agarre ancho."),
            new PasoSeed(2, "Empuje y caída", "Haz una pequeña flexión de piernas y empuja la barra cayendo rápidamente en sentadilla profunda."),
            new PasoSeed(3, "Bloqueo", "Recibe la barra bloqueando codos al fondo de la sentadilla.")
        ));
        Ejercicio squatSnatchEj = saveEjercicio("Arranque Olímpico Completo (Squat Snatch)", "El levantamiento olímpico oficial.");
        seedPasos(squatSnatchEj, List.of(
            new PasoSeed(1, "Tirón triple", "Inicia la salida, pasa rodillas y extiende la cadera con violencia vertical."),
            new PasoSeed(2, "Caída rápida", "Métete abajo de la barra de forma instantánea rompiendo el paralelo."),
            new PasoSeed(3, "Ponerse en pie", "Mantén la barra arriba bloqueada y sube de la sentadilla controladamente.")
        ));
        seedHabilidadEjercicio(squatSnatch, snatchBalance, 1, 3, 3, 35);
        seedHabilidadEjercicio(squatSnatch, squatSnatchEj, 2, 4, 2, 60);

        // --- CLEAN & JERK ---
        Habilidad powerClean = saveHabilidad(cleanJerkHalt, 1, "Cargada de Fuerza / Power Clean", "Levantamiento de la barra al pecho sin sentadilla profunda.", Dificultad.INTERMEDIO, 20);
        Habilidad jerkProg = saveHabilidad(cleanJerkHalt, 2, "Envión / Split Jerk", "Empuje vertical y tijera para fijar la barra arriba.", Dificultad.INTERMEDIO, 25);

        // Ejercicios Power Clean
        Ejercicio frontSquat = saveEjercicio("Sentadilla Frontal (Front Squat)", "Fuerza base de soporte en rack frontal.");
        seedPasos(frontSquat, List.of(
            new PasoSeed(1, "Rack frontal", "Apoya la barra en clavículas/hombros, codos apuntando al frente."),
            new PasoSeed(2, "Bajar", "Desciende manteniendo el torso erguido para no tirar la barra."),
            new PasoSeed(3, "Subir", "Presiona talones manteniendo los codos altos.")
        ));
        Ejercicio powerCleanEj = saveEjercicio("Cargada de Fuerza (Power Clean)", "Llevar la barra del suelo a los hombros.");
        seedPasos(powerCleanEj, List.of(
            new PasoSeed(1, "Primer tirón", "Sube la barra desde el suelo con la espalda plana."),
            new PasoSeed(2, "Segundo tirón", "Extiende la cadera con potencia y rota codos debajo de la barra."),
            new PasoSeed(3, "Recepción", "Atrapa la barra en posición de rack frontal doblando ligeramente las rodillas.")
        ));
        seedHabilidadEjercicio(powerClean, frontSquat, 1, 3, 5, 15);
        seedHabilidadEjercicio(powerClean, powerCleanEj, 2, 3, 3, 35);

        // Ejercicios Jerk
        Ejercicio pushPress = saveEjercicio("Push Press", "Fuerza de empuje asistida con piernas.");
        seedPasos(pushPress, List.of(
            new PasoSeed(1, "Dip", "Dobla rodillas un poco manteniendo el torso perfectamente vertical."),
            new PasoSeed(2, "Drive", "Extiende las piernas con fuerza y empuja la barra sobre la cabeza sin separar los pies.")
        ));
        Ejercicio splitJerk = saveEjercicio("Envión en Tijera (Split Jerk)", "Empuje dividiendo piernas adelante y atrás.");
        seedPasos(splitJerk, List.of(
            new PasoSeed(1, "Dip", "Baja la cadera ligeramente doblando rodillas de forma controlada."),
            new PasoSeed(2, "Explosión", "Empuja la barra y divídela desplazando un pie adelante y el otro atrás."),
            new PasoSeed(3, "Recuperación", "Recibe la barra con brazos trabados, junta pies (delantero primero, luego trasero) y completa el levantamiento.")
        ));
        seedHabilidadEjercicio(jerkProg, pushPress, 1, 3, 5, 20);
        seedHabilidadEjercicio(jerkProg, splitJerk, 2, 3, 2, 45);
        seedHabilidadEjercicio(fondosPecho, fondosEstrictos, 2, 4, 8, 30);

        // Dominadas Básicas
        seedHabilidadEjercicio(dominadasBasicas, domEscapulares, 1, 3, 10, 10);
        seedHabilidadEjercicio(dominadasBasicas, domNegativas, 2, 3, 6, 20);
        seedHabilidadEjercicio(dominadasBasicas, domEstrictas, 3, 4, 5, 35);

        log.info("Asociaciones de Habilidad-Ejercicio creadas.");

        // 6. Usuarios y clasificaciones
        seedUsuariosYClasificaciones(temporada);
        seedEventosComunidad();
    }

    private void seedPasos(Ejercicio ejercicio, List<PasoSeed> pasos) {
        for (PasoSeed p : pasos) {
            pasoRepository.save(Paso.builder()
                    .ejercicio(ejercicio)
                    .orden(p.orden())
                    .nombre(p.nombre())
                    .instruccion(p.instruccion())
                    .build());
        }
    }

    private void seedHabilidadEjercicio(Habilidad habilidad, Ejercicio ejercicio, int orden, int series, int repeticiones, int xp) {
        HabilidadEjercicioKey key = new HabilidadEjercicioKey(habilidad.getId(), ejercicio.getId());
        if (!habilidadEjercicioRepository.existsById(key)) {
            habilidadEjercicioRepository.save(HabilidadEjercicio.builder()
                    .id(key)
                    .habilidad(habilidad)
                    .ejercicio(ejercicio)
                    .orden(orden)
                    .series(series)
                    .repeticiones(repeticiones)
                    .xpOtorgada(xp)
                    .build());
        }
    }

    private void seedUsuariosYClasificaciones(Temporada temporada) {
        Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                .orElseThrow(() -> new IllegalStateException("El rol ADMIN no existe en el sistema."));
        Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                .orElseThrow(() -> new IllegalStateException("El rol USUARIO no existe en el sistema."));

        Liga ligaBronce = ligaRepository.findByOrdenJerarquia(1)
                .orElseThrow(() -> new IllegalStateException("La liga de jerarquía 1 (Bronce) no existe."));

        // Registrar a admin@athletiq.app si no existe
        if (!usuarioRepository.existsByCorreo("admin@athletiq.app")) {
            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .correo("admin@athletiq.app")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(rolAdmin)
                    .nivel(10)
                    .puntosXp(1500)
                    .avatarUrl("https://api.dicebear.com/7.x/adventurer/svg?seed=admin")
                    .colorHexLiga("#B9F2FF")
                    .rachaActual(12)
                    .build();
            usuarioRepository.save(admin);
            log.info("Usuario Administrador 'admin@athletiq.app' registrado con éxito.");
        }

        // Registrar a alex@athletiq.app si no existe
        if (!usuarioRepository.existsByCorreo("alex@athletiq.app")) {
            Usuario alex = Usuario.builder()
                    .nombre("Alex")
                    .correo("alex@athletiq.app")
                    .password(passwordEncoder.encode("password123"))
                    .rol(rolUsuario)
                    .nivel(3)
                    .puntosXp(280)
                    .avatarUrl("https://api.dicebear.com/7.x/adventurer/svg?seed=alex")
                    .colorHexLiga("#CD7F32")
                    .rachaActual(5)
                    .build();
            alex = usuarioRepository.save(alex);
            log.info("Usuario solicitado 'alex@athletiq.app' registrado con éxito.");

            createClasificacion(alex, ligaBronce, temporada, alex.getPuntosXp());
        }

        // Registrar otros usuarios mock para llenar las clasificaciones
        List<MockUserSeed> mockUsers = List.of(
            // Liga Bronce
            new MockUserSeed("Sofía Guerrero", "sofia@athletiq.app", "password123", 5, 540, "https://api.dicebear.com/7.x/adventurer/svg?seed=sofia", 8, "Bronce"),
            new MockUserSeed("Carlos Mendoza", "carlos@athletiq.app", "password123", 2, 190, "https://api.dicebear.com/7.x/adventurer/svg?seed=carlos", 3, "Bronce"),
            new MockUserSeed("Lucía Romero", "lucia@athletiq.app", "password123", 4, 380, "https://api.dicebear.com/7.x/adventurer/svg?seed=lucia", 12, "Bronce"),
            new MockUserSeed("Martín Silva", "martin@athletiq.app", "password123", 1, 80, "https://api.dicebear.com/7.x/adventurer/svg?seed=martin", 1, "Bronce"),
            new MockUserSeed("Juan Pérez", "juan@athletiq.app", "password123", 3, 290, "https://api.dicebear.com/7.x/adventurer/svg?seed=juan", 4, "Bronce"),
            new MockUserSeed("Elena Castro", "elena@athletiq.app", "password123", 2, 150, "https://api.dicebear.com/7.x/adventurer/svg?seed=elena", 2, "Bronce"),
            new MockUserSeed("Pedro Gómez", "pedro@athletiq.app", "password123", 3, 220, "https://api.dicebear.com/7.x/adventurer/svg?seed=pedro", 0, "Bronce"),
            new MockUserSeed("Clara Torres", "clara@athletiq.app", "password123", 4, 340, "https://api.dicebear.com/7.x/adventurer/svg?seed=clara", 7, "Bronce"),
            new MockUserSeed("Miguel Ortega", "miguel@athletiq.app", "password123", 1, 95, "https://api.dicebear.com/7.x/adventurer/svg?seed=miguel", 0, "Bronce"),
            new MockUserSeed("Andrea Vargas", "andrea@athletiq.app", "password123", 5, 490, "https://api.dicebear.com/7.x/adventurer/svg?seed=andrea", 10, "Bronce"),
            
            // Liga Plata
            new MockUserSeed("Mateo Rojas", "mateo@athletiq.app", "password123", 11, 1200, "https://api.dicebear.com/7.x/adventurer/svg?seed=mateo", 4, "Plata"),
            new MockUserSeed("Valentina Peña", "valentina@athletiq.app", "password123", 10, 980, "https://api.dicebear.com/7.x/adventurer/svg?seed=valentina", 2, "Plata"),
            new MockUserSeed("Daniel Herrera", "daniel@athletiq.app", "password123", 9, 920, "https://api.dicebear.com/7.x/adventurer/svg?seed=daniel", 6, "Plata"),
            new MockUserSeed("Camila Fuentes", "camila@athletiq.app", "password123", 8, 780, "https://api.dicebear.com/7.x/adventurer/svg?seed=camila", 3, "Plata"),
            new MockUserSeed("Alejandro Silva", "alejandro@athletiq.app", "password123", 7, 710, "https://api.dicebear.com/7.x/adventurer/svg?seed=alejandro", 9, "Plata"),
            new MockUserSeed("Emily Benítez", "emily@athletiq.app", "password123", 6, 520, "https://api.dicebear.com/7.x/adventurer/svg?seed=emily", 1, "Plata"),
            new MockUserSeed("Ricardo Paz", "ricardo@athletiq.app", "password123", 7, 650, "https://api.dicebear.com/7.x/adventurer/svg?seed=ricardo", 5, "Plata"),
            new MockUserSeed("Paula Medina", "paula@athletiq.app", "password123", 8, 810, "https://api.dicebear.com/7.x/adventurer/svg?seed=paula", 4, "Plata"),
            new MockUserSeed("Oscar Nova", "oscar@athletiq.app", "password123", 9, 900, "https://api.dicebear.com/7.x/adventurer/svg?seed=oscar", 11, "Plata"),
            new MockUserSeed("Natalia Cruz", "natalia@athletiq.app", "password123", 10, 1050, "https://api.dicebear.com/7.x/adventurer/svg?seed=natalia", 15, "Plata"),
            new MockUserSeed("Gabriel Ruiz", "gabriel.ruiz@athletiq.app", "password123", 6, 590, "https://api.dicebear.com/7.x/adventurer/svg?seed=ruiz", 0, "Plata"),
            new MockUserSeed("Diana Moreno", "diana@athletiq.app", "password123", 11, 1150, "https://api.dicebear.com/7.x/adventurer/svg?seed=diana", 8, "Plata"),

            // Liga Oro
            new MockUserSeed("Victoria Sola", "victoria@athletiq.app", "password123", 20, 2450, "https://api.dicebear.com/7.x/adventurer/svg?seed=victoria", 15, "Oro"),
            new MockUserSeed("Francisco Pérez", "francisco@athletiq.app", "password123", 18, 2210, "https://api.dicebear.com/7.x/adventurer/svg?seed=francisco", 11, "Oro"),
            new MockUserSeed("Emma Bianchi", "emma@athletiq.app", "password123", 17, 1980, "https://api.dicebear.com/7.x/adventurer/svg?seed=emma", 20, "Oro"),
            new MockUserSeed("Santino Ferrari", "santino@athletiq.app", "password123", 16, 1840, "https://api.dicebear.com/7.x/adventurer/svg?seed=santino", 7, "Oro"),
            new MockUserSeed("Olivia Rossi", "olivia@athletiq.app", "password123", 14, 1540, "https://api.dicebear.com/7.x/adventurer/svg?seed=olivia", 5, "Oro"),
            new MockUserSeed("Lucas Martin", "lucas@athletiq.app", "password123", 15, 1620, "https://api.dicebear.com/7.x/adventurer/svg?seed=lucas", 10, "Oro"),
            new MockUserSeed("Sara Nelson", "sara@athletiq.app", "password123", 16, 1750, "https://api.dicebear.com/7.x/adventurer/svg?seed=sara", 13, "Oro"),
            new MockUserSeed("Diego Alves", "diego@athletiq.app", "password123", 18, 2100, "https://api.dicebear.com/7.x/adventurer/svg?seed=diego", 6, "Oro"),
            new MockUserSeed("Julia Roberts", "julia@athletiq.app", "password123", 19, 2300, "https://api.dicebear.com/7.x/adventurer/svg?seed=julia", 18, "Oro"),
            new MockUserSeed("Thomas Weber", "thomas@athletiq.app", "password123", 13, 1420, "https://api.dicebear.com/7.x/adventurer/svg?seed=thomas", 2, "Oro"),
            new MockUserSeed("Chloe Dubois", "chloe@athletiq.app", "password123", 15, 1690, "https://api.dicebear.com/7.x/adventurer/svg?seed=chloe", 9, "Oro"),
            new MockUserSeed("Enzo Gallo", "enzo@athletiq.app", "password123", 17, 1910, "https://api.dicebear.com/7.x/adventurer/svg?seed=enzo", 12, "Oro"),

            // Liga Platino
            new MockUserSeed("Bruno Esposito", "bruno@athletiq.app", "password123", 25, 3100, "https://api.dicebear.com/7.x/adventurer/svg?seed=bruno", 14, "Platino"),
            new MockUserSeed("Guillermina Romano", "guillermina@athletiq.app", "password123", 24, 2980, "https://api.dicebear.com/7.x/adventurer/svg?seed=guillermina", 12, "Platino"),
            new MockUserSeed("Lorenzo Colombo", "lorenzo@athletiq.app", "password123", 23, 2750, "https://api.dicebear.com/7.x/adventurer/svg?seed=lorenzo", 8, "Platino"),
            new MockUserSeed("Gemma Ricci", "gemma@athletiq.app", "password123", 22, 2540, "https://api.dicebear.com/7.x/adventurer/svg?seed=gemma", 10, "Platino"),
            new MockUserSeed("Alice Smith", "alice@athletiq.app", "password123", 21, 2510, "https://api.dicebear.com/7.x/adventurer/svg?seed=alice", 5, "Platino"),
            new MockUserSeed("Marcus Aurelius", "marcus@athletiq.app", "password123", 26, 3250, "https://api.dicebear.com/7.x/adventurer/svg?seed=marcus", 25, "Platino"),
            new MockUserSeed("Sophia Loren", "sophial@athletiq.app", "password123", 23, 2800, "https://api.dicebear.com/7.x/adventurer/svg?seed=sophia", 11, "Platino"),
            new MockUserSeed("Rafael Nadal", "rafael@athletiq.app", "password123", 25, 3050, "https://api.dicebear.com/7.x/adventurer/svg?seed=rafael", 17, "Platino"),
            new MockUserSeed("Serena Williams", "serena@athletiq.app", "password123", 27, 3400, "https://api.dicebear.com/7.x/adventurer/svg?seed=serena", 22, "Platino"),
            new MockUserSeed("Karim Benzema", "karim@athletiq.app", "password123", 22, 2690, "https://api.dicebear.com/7.x/adventurer/svg?seed=karim", 4, "Platino"),
            new MockUserSeed("Lionel Messi", "leo@athletiq.app", "password123", 26, 3310, "https://api.dicebear.com/7.x/adventurer/svg?seed=leo", 30, "Platino"),

            // Liga Diamante
            new MockUserSeed("Gabriel Fischer", "gabriel@athletiq.app", "password123", 32, 4550, "https://api.dicebear.com/7.x/adventurer/svg?seed=gabriel", 30, "Diamante"),
            new MockUserSeed("Léa Dubois", "lea@athletiq.app", "password123", 30, 4210, "https://api.dicebear.com/7.x/adventurer/svg?seed=lea", 22, "Diamante"),
            new MockUserSeed("Hugo Bernard", "hugo@athletiq.app", "password123", 28, 3890, "https://api.dicebear.com/7.x/adventurer/svg?seed=hugo", 18, "Diamante"),
            new MockUserSeed("Manon Thomas", "manon@athletiq.app", "password123", 27, 3650, "https://api.dicebear.com/7.x/adventurer/svg?seed=manon", 12, "Diamante"),
            new MockUserSeed("Arthur Pendragon", "arthur@athletiq.app", "password123", 35, 5200, "https://api.dicebear.com/7.x/adventurer/svg?seed=arthur", 45, "Diamante"),
            new MockUserSeed("Guinevere", "guinevere@athletiq.app", "password123", 31, 4400, "https://api.dicebear.com/7.x/adventurer/svg?seed=guinevere", 20, "Diamante"),
            new MockUserSeed("Lancelot", "lancelot@athletiq.app", "password123", 33, 4800, "https://api.dicebear.com/7.x/adventurer/svg?seed=lancelot", 35, "Diamante"),
            new MockUserSeed("Merlin", "merlin@athletiq.app", "password123", 40, 6000, "https://api.dicebear.com/7.x/adventurer/svg?seed=merlin", 60, "Diamante"),
            new MockUserSeed("Morgana Le Fay", "morgana@athletiq.app", "password123", 34, 4650, "https://api.dicebear.com/7.x/adventurer/svg?seed=morgana", 15, "Diamante"),
            new MockUserSeed("Galahad Knight", "galahad@athletiq.app", "password123", 29, 3950, "https://api.dicebear.com/7.x/adventurer/svg?seed=galahad", 8, "Diamante"),
            new MockUserSeed("Percival Pure", "percival@athletiq.app", "password123", 30, 4100, "https://api.dicebear.com/7.x/adventurer/svg?seed=percival", 14, "Diamante"),
            new MockUserSeed("Tristan Sad", "tristan@athletiq.app", "password123", 28, 3750, "https://api.dicebear.com/7.x/adventurer/svg?seed=tristan", 9, "Diamante")
        );

        for (MockUserSeed seed : mockUsers) {
            if (!usuarioRepository.existsByCorreo(seed.correo())) {
                Liga userLiga = ligaRepository.findByNombre(seed.ligaNombre())
                        .orElse(ligaBronce);

                Usuario user = Usuario.builder()
                        .nombre(seed.nombre())
                        .correo(seed.correo())
                        .password(passwordEncoder.encode(seed.password()))
                        .rol(rolUsuario)
                        .nivel(seed.nivel())
                        .puntosXp(seed.xp())
                        .avatarUrl(seed.avatarUrl())
                        .colorHexLiga(userLiga.getColorHex())
                        .rachaActual(seed.racha())
                        .build();
                user = usuarioRepository.save(user);
                log.info("Usuario de prueba '{}' creado en Liga {}.", user.getCorreo(), seed.ligaNombre());

                createClasificacion(user, userLiga, temporada, user.getPuntosXp());
            }
        }
    }

    private void seedEventosComunidad() {
        // Limpiamos los eventos previos para asegurar que se carguen todos los nuevos eventos ricos y diversos
        eventoComunidadRepository.deleteAll();

        List<Usuario> usuarios = usuarioRepository.findAll();
        List<Habilidad> habilidades = habilidadRepository.findAll();
        List<Liga> ligas = ligaRepository.findAll();

        if (usuarios.isEmpty()) return;

        java.util.function.Function<String, Usuario> getUsuario = email -> usuarios.stream()
                .filter(u -> u.getCorreo().equalsIgnoreCase(email))
                .findFirst()
                .orElse(usuarios.get(0));

        // Buscar habilidades
        UUID flexBasicasId = habilidades.stream().filter(h -> h.getNombre().contains("Flexiones")).map(Habilidad::getId).findFirst().orElse(null);
        UUID domBasicasId = habilidades.stream().filter(h -> h.getNombre().contains("Dominadas")).map(Habilidad::getId).findFirst().orElse(null);
        UUID fondosId = habilidades.stream().filter(h -> h.getNombre().contains("Fondos")).map(Habilidad::getId).findFirst().orElse(null);

        // Buscar ligas
        UUID plataId = ligas.stream().filter(l -> l.getNombre().contains("Plata")).map(Liga::getId).findFirst().orElse(null);
        UUID oroId = ligas.stream().filter(l -> l.getNombre().contains("Oro")).map(Liga::getId).findFirst().orElse(null);
        UUID platinoId = ligas.stream().filter(l -> l.getNombre().contains("Platino")).map(Liga::getId).findFirst().orElse(null);
        UUID diamanteId = ligas.stream().filter(l -> l.getNombre().contains("Diamante")).map(Liga::getId).findFirst().orElse(null);

        // Sembrar 35 eventos con desfases de tiempo realistas (horas y días atrás)
        createEvento(getUsuario.apply("gabriel@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, domBasicasId, 0, 1);
        createEvento(getUsuario.apply("alex@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, flexBasicasId, 0, 3);
        createEvento(getUsuario.apply("merlin@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 0, 5);
        createEvento(getUsuario.apply("arthur@athletiq.app"), TipoEvento.LIGA_ASCENSO, diamanteId, 0, 8);
        createEvento(getUsuario.apply("serena@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, fondosId, 0, 12);
        createEvento(getUsuario.apply("leo@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 0, 16);
        createEvento(getUsuario.apply("sofia@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, domBasicasId, 0, 20);
        createEvento(getUsuario.apply("clara@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 1, 2);
        createEvento(getUsuario.apply("valentina@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, flexBasicasId, 1, 6);
        createEvento(getUsuario.apply("mateo@athletiq.app"), TipoEvento.LIGA_ASCENSO, oroId, 1, 10);
        createEvento(getUsuario.apply("lorenzo@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, domBasicasId, 1, 14);
        createEvento(getUsuario.apply("bruno@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 1, 18);
        createEvento(getUsuario.apply("marcus@athletiq.app"), TipoEvento.LIGA_ASCENSO, platinoId, 2, 2);
        createEvento(getUsuario.apply("victoria@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, fondosId, 2, 6);
        createEvento(getUsuario.apply("julia@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 2, 12);
        createEvento(getUsuario.apply("francisco@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, flexBasicasId, 2, 18);
        createEvento(getUsuario.apply("emma@athletiq.app"), TipoEvento.LIGA_ASCENSO, oroId, 3, 1);
        createEvento(getUsuario.apply("andrea@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, domBasicasId, 3, 6);
        createEvento(getUsuario.apply("ricardo@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 3, 12);
        createEvento(getUsuario.apply("natalia@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, flexBasicasId, 3, 18);
        createEvento(getUsuario.apply("diana@athletiq.app"), TipoEvento.LIGA_ASCENSO, plataId, 4, 2);
        createEvento(getUsuario.apply("lucia@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 4, 8);
        createEvento(getUsuario.apply("carlos@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, flexBasicasId, 4, 14);
        createEvento(getUsuario.apply("lea@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, fondosId, 4, 20);
        createEvento(getUsuario.apply("hugo@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 5, 2);
        createEvento(getUsuario.apply("manon@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, domBasicasId, 5, 8);
        createEvento(getUsuario.apply("lancelot@athletiq.app"), TipoEvento.LIGA_ASCENSO, diamanteId, 5, 16);
        createEvento(getUsuario.apply("alice@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 6, 2);
        createEvento(getUsuario.apply("camila@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, flexBasicasId, 6, 10);
        createEvento(getUsuario.apply("alejandro@athletiq.app"), TipoEvento.LIGA_ASCENSO, plataId, 6, 18);
        createEvento(getUsuario.apply("martin@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 7, 2);
        createEvento(getUsuario.apply("pedro@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, domBasicasId, 7, 10);
        createEvento(getUsuario.apply("guinevere@athletiq.app"), TipoEvento.LIGA_ASCENSO, diamanteId, 8, 0);
        createEvento(getUsuario.apply("merlin@athletiq.app"), TipoEvento.HABILIDAD_COMPLETADA, domBasicasId, 9, 0);
        createEvento(getUsuario.apply("tristan@athletiq.app"), TipoEvento.NIVEL_ALCANZADO, null, 10, 0);

        log.info("Eventos de comunidad sembrados.");
    }

    private void createEvento(Usuario usuario, TipoEvento tipo, UUID referenciaId, int diasAtras, int horasAtras) {
        EventoComunidad event = EventoComunidad.builder()
                .usuario(usuario)
                .tipoEvento(tipo)
                .referenciaId(referenciaId)
                .fechaCreacion(LocalDateTime.now().minusDays(diasAtras).minusHours(horasAtras))
                .build();
        eventoComunidadRepository.save(event);
    }

    private void createClasificacion(Usuario usuario, Liga liga, Temporada temporada, int xp) {
        ClasificacionUsuarioKey key = new ClasificacionUsuarioKey(usuario.getId(), liga.getId(), temporada.getId());
        clasificacionUsuarioRepository.save(ClasificacionUsuario.builder()
                .id(key)
                .usuario(usuario)
                .liga(liga)
                .temporada(temporada)
                .xpAcumulada(xp)
                .build());
    }

    private void seedImagesForExistingData() {
        log.info("Iniciando sembrado de imágenes para datos existentes...");
        
        // 1. Actividades
        List<Actividad> actividades = actividadRepository.findAll();
        for (Actividad act : actividades) {
            List<Imagen> imagenes = imagenRepository.findByActividadId(act.getId());
            String url = getImageUrlForActividad(act.getNombre());
            if (imagenes.isEmpty()) {
                imagenRepository.save(Imagen.builder()
                        .urlImagen(url)
                        .nombreArchivo("actividad_" + act.getId() + ".jpg")
                        .descripcion("Imagen de " + act.getNombre())
                        .actividad(act)
                        .build());
            }
        }

        // 2. Habilidades
        List<Habilidad> habilidades = habilidadRepository.findAllWithSeccionAndActividad();
        for (Habilidad hab : habilidades) {
            List<Imagen> imagenes = imagenRepository.findByHabilidadId(hab.getId());
            String actividadNombre = hab.getSeccion() != null && hab.getSeccion().getActividad() != null 
                    ? hab.getSeccion().getActividad().getNombre() 
                    : null;
            String url = getImageUrlForHabilidadYActividad(hab.getNombre(), actividadNombre);
            if (imagenes.isEmpty()) {
                imagenRepository.save(Imagen.builder()
                        .urlImagen(url)
                        .nombreArchivo("habilidad_" + hab.getId() + ".jpg")
                        .descripcion("Imagen de " + hab.getNombre())
                        .habilidad(hab)
                        .build());
            }
        }

        // 3. Ejercicios
        List<Ejercicio> ejercicios = ejercicioRepository.findAll();
        for (Ejercicio ej : ejercicios) {
            List<Imagen> imagenes = imagenRepository.findByEjercicioId(ej.getId());
            List<String> actNombres = habilidadEjercicioRepository.findActividadNombresByEjercicioId(ej.getId());
            String actividadNombre = actNombres.isEmpty() ? null : actNombres.get(0);
            String url = getImageUrlForHabilidadYActividad(ej.getNombre(), actividadNombre);
            if (imagenes.isEmpty()) {
                imagenRepository.save(Imagen.builder()
                        .urlImagen(url)
                        .nombreArchivo("ejercicio_" + ej.getId() + ".jpg")
                        .descripcion("Imagen de " + ej.getNombre())
                        .ejercicio(ej)
                        .build());
            }
        }
        log.info("Imágenes sembradas correctamente.");
    }

    private String getImageUrlForActividad(String name) {
        String n = name.toLowerCase();
        if (n.contains("patinaje")) {
            return "/patinaje_basico.png";
        } else if (n.contains("natacion") || n.contains("natación")) {
            return "/natacion_crol.png";
        } else if (n.contains("running")) {
            return "/running_resistencia.png";
        } else if (n.contains("calistenia")) {
            return "/calistenia_core.png";
        } else if (n.contains("yoga")) {
            return "/yoga_balance.png";
        } else if (n.contains("gimnasia")) {
            return "/calistenia_handstand.png";
        } else if (n.contains("halterofilia")) {
            return "/calistenia_piernas.png";
        }
        return "/habilidad_ejercicio_placeholder.jpg";
    }

    private String getImageUrlForHabilidadYActividad(String name, String actividadNombre) {
        if (actividadNombre == null) {
            return getImageUrlForHabilidad(name);
        }
        
        String act = actividadNombre.toLowerCase();
        String n = name.toLowerCase();
        
        if (act.contains("patinaje")) {
            if (n.contains("freno") || n.contains("detención") || n.contains("detener") || n.contains("t-stop") || n.contains("taco")) {
                return "/patinaje_freno.png";
            }
            if (n.contains("postura") || n.contains("seguridad") || n.contains("ready")) {
                return "/patinaje_postura.png";
            }
            return "/patinaje_basico.png";
        }
        
        if (act.contains("natacion") || act.contains("natación")) {
            if (n.contains("pecho") || n.contains("rana") || n.contains("coordinación")) {
                return "/natacion_pecho.png";
            }
            return "/natacion_crol.png";
        }
        
        if (act.contains("running")) {
            if (n.contains("aeróbica") || n.contains("resistencia") || n.contains("fartlek") || n.contains("intervalo") || n.contains("zona 2") || n.contains("zona2")) {
                return "/running_resistencia.png";
            }
            return "/running_tecnica.png";
        }
        
        if (act.contains("calistenia")) {
            if (n.contains("flexión") || n.contains("pushup") || n.contains("push-up") || n.contains("inclinada") || n.contains("regular") || n.contains("pica") || n.contains("hspu")) {
                return "/calistenia_flexion.png";
            }
            if (n.contains("fondo") || n.contains("dips")) {
                return "/calistenia_fondo.png";
            }
            if (n.contains("handstand") || n.contains("parada de manos") || n.contains("vertical")) {
                return "/calistenia_handstand.png";
            }
            if (n.contains("muscle-up") || n.contains("muscleup") || n.contains("explosiva") || n.contains("transición")) {
                return "/calistenia_muscleup.png";
            }
            if (n.contains("remo") || n.contains("dominada") || n.contains("pullup") || n.contains("escapular") || n.contains("tracción")) {
                return "/calistenia_dominada.png";
            }
            if (n.contains("sentadilla") || n.contains("squat") || n.contains("pistol") || n.contains("búlgara") || n.contains("piernas") || n.contains("tren inferior")) {
                return "/calistenia_piernas.png";
            }
            return "/calistenia_core.png";
        }
        
        if (act.contains("yoga")) {
            if (n.contains("cadera") || n.contains("apertura") || n.contains("split") || n.contains("paloma") || n.contains("mariposa") || n.contains("baddha") || n.contains("kapotasana") || n.contains("hanumanasana")) {
                return "/yoga_cadera.png";
            }
            if (n.contains("arco") || n.contains("rueda") || n.contains("columna") || n.contains("cobra") || n.contains("espalda") || n.contains("dhanurasana") || n.contains("chakrasana") || n.contains("bhujangasana")) {
                return "/yoga_espalda.png";
            }
            return "/yoga_balance.png";
        }
        
        if (act.contains("gimnasia")) {
            if (n.contains("handstand") || n.contains("parada de manos") || n.contains("vertical") || n.contains("caminata") || n.contains("wall") || n.contains("kick-up")) {
                return "/calistenia_handstand.png";
            }
            if (n.contains("fondo")) {
                return "/calistenia_fondo.png";
            }
            return "/calistenia_core.png";
        }
        
        if (act.contains("halterofilia")) {
            if (n.contains("sentadilla") || n.contains("squat") || n.contains("frontal")) {
                return "/calistenia_piernas.png";
            }
            return "/calistenia_piernas.png";
        }
        
        return getImageUrlForHabilidad(name);
    }

    private String getImageUrlForHabilidad(String name) {
        String n = name.toLowerCase();
        if (n.contains("apertura") || n.contains("flexibilidad de cadera") || n.contains("split") || n.contains("paloma") || n.contains("mariposa")) {
            return "/yoga_cadera.png";
        } else if (n.contains("cuervo") || n.contains("bakasana") || n.contains("balance en antebrazos") || n.contains("pincha")) {
            return "/yoga_balance.png";
        } else if (n.contains("arco") || n.contains("rueda") || n.contains("chakrasana") || n.contains("cobra") || n.contains("columna") || n.contains("dhanurasana")) {
            return "/yoga_espalda.png";
        } else if (n.contains("postura de seguridad")) {
            return "/patinaje_postura.png";
        } else if (n.contains("limón") || n.contains("swizzle")) {
            return "/patinaje_basico.png";
        } else if (n.contains("zancada básica") || n.contains("basic stride")) {
            return "/patinaje_basico.png";
        } else if (n.contains("desplazamiento") || n.contains("crossover") || n.contains("cruces") || n.contains("zancada")) {
            return "/patinaje_basico.png";
        } else if (n.contains("freno de taco")) {
            return "/patinaje_freno.png";
        } else if (n.contains("freno en t") || n.contains("t-stop")) {
            return "/patinaje_freno.png";
        } else if (n.contains("freno") || n.contains("giro") || n.contains("tijera") || n.contains("detención") || n.contains("dirección")) {
            return "/patinaje_freno.png";
        } else if (n.contains("crol") || n.contains("propulsión") || n.contains("pecho") || n.contains("rana") || n.contains("natación") || n.contains("acuático")) {
            return "/natacion_crol.png";
        } else if (n.contains("viraje") || n.contains("campana") || n.contains("salida") || n.contains("flip")) {
            return "/natacion_pecho.png";
        } else if (n.contains("postura y cadencia") || n.contains("zancada y apoyo") || n.contains("skipping") || n.contains("ppm") || n.contains("metatarso")) {
            return "/running_tecnica.png";
        } else if (n.contains("aeróbica") || n.contains("fartlek") || n.contains("intervalo") || n.contains("resistencia") || n.contains("zona 2")) {
            return "/running_resistencia.png";
        } else if (n.contains("flexión") || n.contains("pushup") || n.contains("pica")) {
            if (n.contains("parada") || n.contains("hspu") || n.contains("vertical")) {
                return "/calistenia_handstand.png";
            }
            return "/calistenia_flexion.png";
        } else if (n.contains("fondo") || n.contains("dips")) {
            return "/calistenia_fondo.png";
        } else if (n.contains("handstand") || n.contains("parada de manos") || n.contains("vertical")) {
            return "/calistenia_handstand.png";
        } else if (n.contains("remo") || n.contains("dominada") || n.contains("pullup") || n.contains("escapular") || n.contains("tracción")) {
            if (n.contains("muscle")) {
                return "/calistenia_muscleup.png";
            }
            return "/calistenia_dominada.png";
        } else if (n.contains("muscle-up") || n.contains("explosiva") || n.contains("transición")) {
            return "/calistenia_muscleup.png";
        } else if (n.contains("plancha") || n.contains("l-sit") || n.contains("lever") || n.contains("tuck") || n.contains("core") || n.contains("soporte") || n.contains("rodillas colgado")) {
            return "/calistenia_core.png";
        } else if (n.contains("sentadilla") || n.contains("squat") || n.contains("pistol") || n.contains("búlgara") || n.contains("piernas") || n.contains("tren inferior")) {
            return "/calistenia_piernas.png";
        }
        return "/habilidad_ejercicio_placeholder.jpg";
    }

    // ── Registro interno ─────────────────────────────────────────────────────

    private record LigaSeed(String nombre, int orden, String colorHex) {}
    private record PasoSeed(int orden, String nombre, String instruccion) {}
    private record MockUserSeed(String nombre, String correo, String password, int nivel, int xp, String avatarUrl, int racha, String ligaNombre) {}
}
