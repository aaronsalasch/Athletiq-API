package com.athletiq.backend.controllers;

import com.athletiq.backend.models.entities.*;
import com.athletiq.backend.models.enums.Dificultad;
import com.athletiq.backend.repositories.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    private final ActividadRepository actividadRepository;
    private final SeccionRepository seccionRepository;
    private final HabilidadRepository habilidadRepository;
    private final EjercicioRepository ejercicioRepository;
    private final ImagenRepository imagenRepository;
    private final HabilidadEjercicioRepository habilidadEjercicioRepository;

    // =========================================================================
    // 1. ACTIVIDADES CRUD
    // =========================================================================

    @GetMapping("/actividades")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ActividadAdminResponse>> listarActividades() {
        List<ActividadAdminResponse> res = actividadRepository.findAll().stream()
                .map(this::mapToActividadResponse)
                .sorted(java.util.Comparator.comparing(ActividadAdminResponse::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/actividades")
    @Transactional
    public ResponseEntity<ActividadAdminResponse> crearActividad(@RequestBody ActividadDTO dto) {
        Actividad act = Actividad.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
        
        if (dto.getImagenes() != null) {
            for (ImagenDTO imgDto : dto.getImagenes()) {
                Imagen img = Imagen.builder()
                        .urlImagen(imgDto.getUrlImagen())
                        .nombreArchivo(imgDto.getNombreArchivo())
                        .descripcion(imgDto.getDescripcion())
                        .actividad(act)
                        .build();
                act.getImagenes().add(img);
            }
        }
        
        Actividad guardada = actividadRepository.save(act);
        return ResponseEntity.ok(mapToActividadResponse(guardada));
    }

    @PutMapping("/actividades/{id}")
    @Transactional
    public ResponseEntity<ActividadAdminResponse> actualizarActividad(@PathVariable UUID id, @RequestBody ActividadDTO dto) {
        Actividad act = actividadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
        
        act.setNombre(dto.getNombre());
        act.setDescripcion(dto.getDescripcion());
        
        act.getImagenes().clear();
        if (dto.getImagenes() != null) {
            for (ImagenDTO imgDto : dto.getImagenes()) {
                Imagen img = Imagen.builder()
                        .urlImagen(imgDto.getUrlImagen())
                        .nombreArchivo(imgDto.getNombreArchivo())
                        .descripcion(imgDto.getDescripcion())
                        .actividad(act)
                        .build();
                act.getImagenes().add(img);
            }
        }
        
        Actividad guardada = actividadRepository.save(act);
        return ResponseEntity.ok(mapToActividadResponse(guardada));
    }

    @DeleteMapping("/actividades/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarActividad(@PathVariable UUID id) {
        if (!actividadRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        actividadRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // 2. SECCIONES CRUD
    // =========================================================================

    @GetMapping("/secciones")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SeccionAdminResponse>> listarSecciones() {
        List<SeccionAdminResponse> res = seccionRepository.findAll().stream()
                .map(this::mapToSeccionResponse)
                .sorted(java.util.Comparator.comparing((SeccionAdminResponse s) -> s.getNombreActividad() != null ? s.getNombreActividad() : "", String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(SeccionAdminResponse::getOrden)
                        .thenComparing(SeccionAdminResponse::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/secciones")
    @Transactional
    public ResponseEntity<SeccionAdminResponse> crearSeccion(@RequestBody SeccionDTO dto) {
        Actividad act = actividadRepository.findById(dto.getIdActividad())
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));

        Seccion sec = Seccion.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .orden(dto.getOrden())
                .actividad(act)
                .build();

        Seccion guardada = seccionRepository.save(sec);
        return ResponseEntity.ok(mapToSeccionResponse(guardada));
    }

    @PutMapping("/secciones/{id}")
    @Transactional
    public ResponseEntity<SeccionAdminResponse> actualizarSeccion(@PathVariable UUID id, @RequestBody SeccionDTO dto) {
        Seccion sec = seccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada"));

        Actividad act = actividadRepository.findById(dto.getIdActividad())
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));

        sec.setNombre(dto.getNombre());
        sec.setDescripcion(dto.getDescripcion());
        sec.setOrden(dto.getOrden());
        sec.setActividad(act);

        Seccion guardada = seccionRepository.save(sec);
        return ResponseEntity.ok(mapToSeccionResponse(guardada));
    }

    @DeleteMapping("/secciones/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarSeccion(@PathVariable UUID id) {
        if (!seccionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        seccionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // 3. HABILIDADES CRUD
    // =========================================================================

    @GetMapping("/habilidades")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HabilidadAdminResponse>> listarHabilidades() {
        List<HabilidadAdminResponse> res = habilidadRepository.findAll().stream()
                .map(this::mapToHabilidadResponse)
                .sorted(java.util.Comparator.comparing((HabilidadAdminResponse h) -> h.getNombreSeccion() != null ? h.getNombreSeccion() : "", String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(HabilidadAdminResponse::getOrden)
                        .thenComparing(HabilidadAdminResponse::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/habilidades")
    @Transactional
    public ResponseEntity<HabilidadAdminResponse> crearHabilidad(@RequestBody HabilidadDTO dto) {
        Seccion sec = seccionRepository.findById(dto.getIdSeccion())
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada"));

        Habilidad hab = Habilidad.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .orden(dto.getOrden())
                .dificultad(mapDificultadToEnum(dto.getDificultad()))
                .tiempoEstimado(dto.getTiempoEstimado())
                .seccion(sec)
                .build();

        if (dto.getImagenes() != null) {
            for (ImagenDTO imgDto : dto.getImagenes()) {
                Imagen img = Imagen.builder()
                        .urlImagen(imgDto.getUrlImagen())
                        .nombreArchivo(imgDto.getNombreArchivo())
                        .descripcion(imgDto.getDescripcion())
                        .habilidad(hab)
                        .build();
                hab.getImagenes().add(img);
            }
        }

        Habilidad guardada = habilidadRepository.saveAndFlush(hab);
        guardarRelacionesEjercicio(guardada, dto.getEjercicios());
        return ResponseEntity.ok(mapToHabilidadResponse(guardada));
    }

    @PutMapping("/habilidades/{id}")
    @Transactional
    public ResponseEntity<HabilidadAdminResponse> actualizarHabilidad(@PathVariable UUID id, @RequestBody HabilidadDTO dto) {
        Habilidad hab = habilidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Habilidad no encontrada"));

        Seccion sec = seccionRepository.findById(dto.getIdSeccion())
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada"));

        hab.setNombre(dto.getNombre());
        hab.setDescripcion(dto.getDescripcion());
        hab.setOrden(dto.getOrden());
        hab.setDificultad(mapDificultadToEnum(dto.getDificultad()));
        hab.setTiempoEstimado(dto.getTiempoEstimado());
        hab.setSeccion(sec);

        hab.getImagenes().clear();
        if (dto.getImagenes() != null) {
            for (ImagenDTO imgDto : dto.getImagenes()) {
                Imagen img = Imagen.builder()
                        .urlImagen(imgDto.getUrlImagen())
                        .nombreArchivo(imgDto.getNombreArchivo())
                        .descripcion(imgDto.getDescripcion())
                        .habilidad(hab)
                        .build();
                hab.getImagenes().add(img);
            }
        }

        Habilidad guardada = habilidadRepository.saveAndFlush(hab);
        guardarRelacionesEjercicio(guardada, dto.getEjercicios());
        return ResponseEntity.ok(mapToHabilidadResponse(guardada));
    }

    @DeleteMapping("/habilidades/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarHabilidad(@PathVariable UUID id) {
        if (!habilidadRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        habilidadRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void guardarRelacionesEjercicio(Habilidad hab, List<HabilidadEjercicioDTO> ejerciciosDto) {
        // 1. Eliminar relaciones previas
        List<HabilidadEjercicio> previas = habilidadEjercicioRepository.findByHabilidadIdOrderByOrden(hab.getId());
        if (!previas.isEmpty()) {
            habilidadEjercicioRepository.deleteAll(previas);
            habilidadEjercicioRepository.flush();
        }

        // 2. Insertar nuevas relaciones si existen
        if (ejerciciosDto != null) {
            for (HabilidadEjercicioDTO heDto : ejerciciosDto) {
                Ejercicio ej = ejercicioRepository.findById(heDto.getIdEjercicio())
                        .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado: " + heDto.getIdEjercicio()));

                HabilidadEjercicio keyRelation = HabilidadEjercicio.builder()
                        .id(new com.athletiq.backend.models.keys.HabilidadEjercicioKey(hab.getId(), ej.getId()))
                        .habilidad(hab)
                        .ejercicio(ej)
                        .orden(heDto.getOrden())
                        .series(heDto.getSeries())
                        .repeticiones(heDto.getRepeticiones())
                        .xpOtorgada(heDto.getXpOtorgada() != null ? heDto.getXpOtorgada() : 0)
                        .build();

                habilidadEjercicioRepository.save(keyRelation);
            }
        }
    }

    // =========================================================================
    // 4. EJERCICIOS CRUD
    // =========================================================================

    @GetMapping("/ejercicios")
    @Transactional(readOnly = true)
    public ResponseEntity<List<EjercicioAdminResponse>> listarEjercicios() {
        List<EjercicioAdminResponse> res = ejercicioRepository.findAll().stream()
                .map(this::mapToEjercicioResponse)
                .sorted(java.util.Comparator.comparing(EjercicioAdminResponse::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/ejercicios")
    @Transactional
    public ResponseEntity<EjercicioAdminResponse> crearEjercicio(@RequestBody EjercicioDTO dto) {
        Ejercicio ej = Ejercicio.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();

        if (dto.getImagenes() != null) {
            for (ImagenDTO imgDto : dto.getImagenes()) {
                Imagen img = Imagen.builder()
                        .urlImagen(imgDto.getUrlImagen())
                        .nombreArchivo(imgDto.getNombreArchivo())
                        .descripcion(imgDto.getDescripcion())
                        .ejercicio(ej)
                        .build();
                ej.getImagenes().add(img);
            }
        }

        Ejercicio guardado = ejercicioRepository.save(ej);
        return ResponseEntity.ok(mapToEjercicioResponse(guardado));
    }

    @PutMapping("/ejercicios/{id}")
    @Transactional
    public ResponseEntity<EjercicioAdminResponse> actualizarEjercicio(@PathVariable UUID id, @RequestBody EjercicioDTO dto) {
        Ejercicio ej = ejercicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ejercicio no encontrado"));

        ej.setNombre(dto.getNombre());
        ej.setDescripcion(dto.getDescripcion());

        ej.getImagenes().clear();
        if (dto.getImagenes() != null) {
            for (ImagenDTO imgDto : dto.getImagenes()) {
                Imagen img = Imagen.builder()
                        .urlImagen(imgDto.getUrlImagen())
                        .nombreArchivo(imgDto.getNombreArchivo())
                        .descripcion(imgDto.getDescripcion())
                        .ejercicio(ej)
                        .build();
                ej.getImagenes().add(img);
            }
        }

        Ejercicio guardado = ejercicioRepository.save(ej);
        return ResponseEntity.ok(mapToEjercicioResponse(guardado));
    }

    @DeleteMapping("/ejercicios/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarEjercicio(@PathVariable UUID id) {
        if (!ejercicioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ejercicioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // 5. IMÁGENES UPLOAD
    // =========================================================================

    @PostMapping("/imagenes/upload")
    @Transactional
    public ResponseEntity<ImagenResponse> subirImagen(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        try {
            java.io.File uploadDir = new java.io.File("./uploads");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            java.nio.file.Path path = java.nio.file.Paths.get("./uploads/" + fileName);
            java.nio.file.Files.write(path, file.getBytes());

            String fileUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();

            ImagenResponse response = ImagenResponse.builder()
                    .urlImagen(fileUrl)
                    .nombreArchivo(originalFilename != null ? originalFilename : fileName)
                    .descripcion("")
                    .build();

            return ResponseEntity.ok(response);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }


    // =========================================================================
    // MAPPERS & UTILS
    // =========================================================================

    private Dificultad mapDificultadToEnum(String dif) {
        if (dif == null) return Dificultad.PRINCIPIANTE;
        String d = dif.toLowerCase();
        if (d.contains("fácil") || d.contains("facil") || d.contains("principiante")) {
            return Dificultad.PRINCIPIANTE;
        } else if (d.contains("medio") || d.contains("intermedio")) {
            return Dificultad.INTERMEDIO;
        } else if (d.contains("difícil") || d.contains("dificil") || d.contains("avanzado")) {
            return Dificultad.AVANZADO;
        } else if (d.contains("experto")) {
            return Dificultad.EXPERTO;
        }
        return Dificultad.PRINCIPIANTE;
    }

    private String mapEnumToDificultad(Dificultad dif) {
        if (dif == null) return "Fácil";
        switch (dif) {
            case PRINCIPIANTE: return "Fácil";
            case INTERMEDIO: return "Medio";
            case AVANZADO: return "Difícil";
            case EXPERTO: return "Experto";
            default: return "Fácil";
        }
    }

    private ActividadAdminResponse mapToActividadResponse(Actividad act) {
        return ActividadAdminResponse.builder()
                .id(act.getId())
                .nombre(act.getNombre())
                .descripcion(act.getDescripcion())
                .imagenes(act.getImagenes().stream().map(this::mapToImagenResponse).collect(Collectors.toList()))
                .build();
    }

    private SeccionAdminResponse mapToSeccionResponse(Seccion sec) {
        return SeccionAdminResponse.builder()
                .id(sec.getId())
                .nombre(sec.getNombre())
                .descripcion(sec.getDescripcion())
                .orden(sec.getOrden())
                .idActividad(sec.getActividad() != null ? sec.getActividad().getId() : null)
                .nombreActividad(sec.getActividad() != null ? sec.getActividad().getNombre() : null)
                .build();
    }

    private HabilidadAdminResponse mapToHabilidadResponse(Habilidad hab) {
        List<HabilidadEjercicioDTO> ejs = habilidadEjercicioRepository.findByHabilidadIdOrderByOrden(hab.getId()).stream()
                .map(he -> HabilidadEjercicioDTO.builder()
                        .idEjercicio(he.getEjercicio().getId())
                        .nombreEjercicio(he.getEjercicio().getNombre())
                        .orden(he.getOrden())
                        .series(he.getSeries())
                        .repeticiones(he.getRepeticiones())
                        .xpOtorgada(he.getXpOtorgada())
                        .build())
                .collect(Collectors.toList());

        return HabilidadAdminResponse.builder()
                .id(hab.getId())
                .nombre(hab.getNombre())
                .descripcion(hab.getDescripcion())
                .orden(hab.getOrden())
                .dificultad(mapEnumToDificultad(hab.getDificultad()))
                .tiempoEstimado(hab.getTiempoEstimado())
                .idSeccion(hab.getSeccion() != null ? hab.getSeccion().getId() : null)
                .nombreSeccion(hab.getSeccion() != null ? hab.getSeccion().getNombre() : null)
                .imagenes(hab.getImagenes().stream().map(this::mapToImagenResponse).collect(Collectors.toList()))
                .ejercicios(ejs)
                .build();
    }

    private EjercicioAdminResponse mapToEjercicioResponse(Ejercicio ej) {
        return EjercicioAdminResponse.builder()
                .id(ej.getId())
                .nombre(ej.getNombre())
                .descripcion(ej.getDescripcion())
                .imagenes(ej.getImagenes().stream().map(this::mapToImagenResponse).collect(Collectors.toList()))
                .build();
    }

    private ImagenResponse mapToImagenResponse(Imagen img) {
        return ImagenResponse.builder()
                .id(img.getId())
                .urlImagen(img.getUrlImagen())
                .nombreArchivo(img.getNombreArchivo())
                .descripcion(img.getDescripcion())
                .build();
    }

    // =========================================================================
    // DTO INNER CLASSES
    // =========================================================================

    @Data
    public static class ImagenDTO {
        private String urlImagen;
        private String nombreArchivo;
        private String descripcion;
    }

    @Data
    public static class ActividadDTO {
        private String nombre;
        private String descripcion;
        private List<ImagenDTO> imagenes;
    }

    @Data
    public static class SeccionDTO {
        private String nombre;
        private String descripcion;
        private Integer orden;
        private UUID idActividad;
    }

    @Data
    public static class HabilidadDTO {
        private String nombre;
        private String descripcion;
        private Integer orden;
        private String dificultad;
        private Integer tiempoEstimado;
        private UUID idSeccion;
        private List<ImagenDTO> imagenes;
        private List<HabilidadEjercicioDTO> ejercicios;
    }

    @Data
    public static class EjercicioDTO {
        private String nombre;
        private String descripcion;
        private List<ImagenDTO> imagenes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActividadAdminResponse {
        private UUID id;
        private String nombre;
        private String descripcion;
        private List<ImagenResponse> imagenes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeccionAdminResponse {
        private UUID id;
        private String nombre;
        private String descripcion;
        private Integer orden;
        private UUID idActividad;
        private String nombreActividad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HabilidadAdminResponse {
        private UUID id;
        private String nombre;
        private String descripcion;
        private Integer orden;
        private String dificultad;
        private Integer tiempoEstimado;
        private UUID idSeccion;
        private String nombreSeccion;
        private List<ImagenResponse> imagenes;
        private List<HabilidadEjercicioDTO> ejercicios;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HabilidadEjercicioDTO {
        private UUID idEjercicio;
        private String nombreEjercicio;
        private Integer orden;
        private Integer series;
        private Integer repeticiones;
        private Integer xpOtorgada;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EjercicioAdminResponse {
        private UUID id;
        private String nombre;
        private String descripcion;
        private List<ImagenResponse> imagenes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImagenResponse {
        private UUID id;
        private String urlImagen;
        private String nombreArchivo;
        private String descripcion;
    }
}
