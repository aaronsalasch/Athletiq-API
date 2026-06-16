# Athletiq Backend API

API RESTful para la plataforma gamificada de aprendizaje de habilidades físicas.  
Construida con **Spring Boot 3.3**, **Spring Security + JWT**, **Spring Data JPA** y **PostgreSQL**.

---

## Tabla de contenidos

1. [Stack tecnológico](#stack-tecnológico)
2. [Prerequisitos](#prerequisitos)
3. [Configuración inicial](#configuración-inicial)
4. [Cómo arrancar](#cómo-arrancar)
5. [Cómo correr los tests](#cómo-correr-los-tests)
6. [Arquitectura de capas](#arquitectura-de-capas)
7. [Modelo de datos](#modelo-de-datos)
8. [Motores de lógica de negocio](#motores-de-lógica-de-negocio)
9. [Seguridad](#seguridad)
10. [Endpoints](#endpoints)
11. [Swagger UI](#swagger-ui)
12. [Variables de entorno / propiedades](#variables-de-entorno--propiedades)

---

## Stack tecnológico

|      Capa     |                Tecnología                   |
|---------------|---------------------------------------------|
| Lenguaje      | Java 21                                     |
| Framework     | Spring Boot 3.3.4                           |
| Persistencia  | Spring Data JPA + Hibernate 6               |  
| Base de datos | PostgreSQL 15+                              |
| Seguridad     | Spring Security 6 + JWT (JJWT 0.12)         |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI)            |
| Build         | Maven 3.9+                                  |
| Boilerplate   | Lombok                                      |
| Tests         | JUnit 5 + Spring Boot Test + H2 (in-memory) |

---

## Prerequisitos

Antes de arrancar necesitas tener instalado:

- **Java 21** — [Descargar](https://adoptium.net/)
- **Maven 3.9+** — [Descargar](https://maven.apache.org/download.cgi) (o usar el wrapper `mvnw` incluido)
- **PostgreSQL 15+** — [Descargar](https://www.postgresql.org/download/)

Verifica las instalaciones:

```bash
java -version      # debe mostrar 21.x.x
mvn -version       # debe mostrar 3.9.x
psql --version     # debe mostrar 15.x o superior
```

---

## Configuración inicial

### 1. Crear la base de datos

Abre `psql` (o pgAdmin) y ejecuta:

```sql
CREATE DATABASE athletiq_db;

-- Opcional: crea un usuario dedicado
CREATE USER athletiq_user WITH PASSWORD 'root';
GRANT ALL PRIVILEGES ON DATABASE athletiq_db TO athletiq_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO athletiq_user;
ALTER DATABASE athletiq_db OWNER TO athletiq_user;
```

### 2. Configurar las propiedades

Edita el archivo `src/main/resources/application.properties`:

```properties
# Cambia estos valores según tu entorno
spring.datasource.url=jdbc:postgresql://localhost:5432/athletiq_db
spring.datasource.username=athletiq_db
spring.datasource.password=root

# Genera un secret Base64 de al menos 32 caracteres
# Puedes usar: openssl rand -base64 32
athletiq.jwt.secret=TU_SECRET_AQUI_MINIMO_32_CARACTERES_EN_BASE64
```

> **Nota:** `spring.jpa.hibernate.ddl-auto=update` crea las tablas automáticamente en el primer arranque.  
> Para producción, cámbialo a `validate` y usa Flyway/Liquibase para migraciones.

### 3. Datos iniciales (automático)

Al arrancar, el `DataInitializer` siembra automáticamente:

|    Dato   |                 Valores                   |
|-----------|-------------------------------------------|
| Roles     | `USUARIO`, `ADMIN`, `INVITADO`            |
| Ligas     | Bronce → Plata → Oro → Platino → Diamante |
| Temporada | Primera temporada activa desde hoy        |

No necesitas insertar nada manualmente.

---

## Cómo arrancar

### Opción A — Maven directo

```bash
cd backend

# Compilar y arrancar
mvn spring-boot:run

# O compilar primero y luego ejecutar el JAR
mvn clean package -DskipTests
java -jar target/athletiq-backend-0.0.1-SNAPSHOT.jar
```

### Opción B — IDE (IntelliJ / Eclipse / VS Code)

1. Abre la carpeta `backend/` como proyecto Maven.
2. Espera a que se descarguen las dependencias.
3. Ejecuta la clase `AthletiqBackendApplication.java`.

### Verificar que arrancó

```
http://localhost:8080/swagger-ui.html   → Swagger UI
http://localhost:8080/api-docs          → JSON de OpenAPI
```

Si ves la interfaz de Swagger, el backend está corriendo correctamente.

---

## Cómo correr los tests

Los tests usan **H2 en memoria** — no necesitan PostgreSQL.

```bash
# Todos los tests
mvn test

# Solo una suite específica
mvn test -Dtest=AuthControllerIntegrationTest
mvn test -Dtest=ProgresoServiceIntegrationTest
mvn test -Dtest=GamificacionServiceTest
```

Las suites disponibles:

|              Suite               |                        Qué prueba                                |
|----------------------------------|------------------------------------------------------------------|
| `AuthControllerIntegrationTest`  | Register, login, credenciales inválidas, email duplicado         |
| `ProgresoServiceIntegrationTest` | Motor de Rachas, idempotencia, completitud de habilidad          |
| `GamificacionServiceTest`        | Fórmula de nivel, acumulación de XP, TransaccionXp, idempotencia |

---

## Arquitectura de capas

```
com.athletiq.backend
│
├── controllers/          # Capa HTTP — reciben requests, retornan DTOs
├── services/
│   ├── (interfaces)      # Contratos de negocio
│   ├── impl/             # Implementaciones con lógica de negocio
│   └── scheduled/        # Cron Jobs (@Scheduled)
├── repositories/         # Spring Data JPA — acceso a BD
├── models/
│   ├── entities/         # Entidades JPA (@Entity)
│   ├── keys/             # Claves compuestas (@Embeddable)
│   └── enums/            # Enumeraciones (Dificultad, TipoEvento)
├── dtos/
│   ├── request/          # Objetos de entrada (con validación @Valid)
│   └── response/         # Objetos de salida (nunca entidades JPA directas)
├── security/             # JWT filter, UserDetailsService, SecurityUtils
├── config/               # SecurityConfig, AsyncConfig, OpenApiConfig, DataInitializer
└── exceptions/           # GlobalExceptionHandler + excepciones personalizadas
```

**Reglas de diseño:**
- Los `@Controller` nunca retornan entidades JPA — solo DTOs.
- Los `@Service` son la única capa que contiene lógica de negocio.
- Los `@Repository` solo contienen consultas, sin lógica.
- Las relaciones N:M con atributos extra usan `@EmbeddedId` explícito (sin `@ManyToMany`).

---

## Modelo de datos

### Entidades principales

```
Rol ──────────── Usuario
                    │
         ┌──────────┼──────────────────┐
         │          │                  │
    ProgresoHabilidad  ProgresoEjercicio  ClasificacionUsuario
         │          │                  │
      Habilidad  Ejercicio           Liga + Temporada
         │          │
      Seccion    HabilidadEjercicio
         │          (tabla intermedia con orden, series, rep, xp)
      Actividad

TransaccionXp ── Usuario + Habilidad (nullable)
EventoComunidad ── Usuario
```

### Claves compuestas (`@EmbeddedId`)

|        Entidad         |              Campos de la clave                |
|------------------------|------------------------------------------------|
| `HabilidadEjercicio`   | `id_habilidad` + `id_ejercicio`                |
| `ProgresoHabilidad`    | `id_usuario` + `id_habilidad`                  |
| `ProgresoEjercicio`    | `id_usuario` + `id_habilidad` + `id_ejercicio` |
| `ClasificacionUsuario` | `id_usuario` + `id_liga` + `id_temporada`      |

Todas las PKs individuales usan `UUID` generado por Hibernate (`@GeneratedValue(strategy = GenerationType.UUID)`).

---

## Motores de lógica de negocio

### 1. Motor de Rachas (`ProgresoServiceImpl`)

Se activa cada vez que un usuario completa un ejercicio.

```
fechaUltimaAct == null      → rachaActual = 1
diferencia == 0 días        → misma sesión del día, sin cambio
diferencia == 1 día         → rachaActual + 1
diferencia > 1 día          → rachaActual = 1  (racha rota)
```

### 2. Motor de Gamificación (`GamificacionServiceImpl`)

Se activa tras cada ejercicio completado:

```
1. Cuenta ejercicios completados vs total de la habilidad
2. Si todos completados → marca ProgresoHabilidad.completado = true
3. Suma XP de todos los ejercicios (campo xp_otorgada)
4. Inserta TransaccionXp
5. Actualiza Usuario.puntosXp y recalcula nivel
6. Actualiza ClasificacionUsuario.xpAcumulada (temporada activa)
7. Dispara eventos asíncronos si hubo nivel up / habilidad completada
```

**Fórmula de nivel:**
```
nivel = floor(√(puntosXp / 100)) + 1

Nivel 1: 0–99 XP
Nivel 2: 100–399 XP
Nivel 3: 400–899 XP
Nivel 4: 900–1599 XP  …
```

### 3. Cron Job de Ligas (`LigaSchedulerService`)

Ejecuta **cada domingo a medianoche** (`cron = "0 0 0 * * SUN"`):

```
1. Cierra Temporada activa (activa=false, fechaFin=hoy)
2. Lee ranking global ordenado por xp_acumulada DESC
3. Redistribuye ligas por percentil (top 20% → liga superior, etc.)
4. Actualiza colorHexLiga en cada Usuario
5. Publica eventos de ascenso de liga (asíncrono)
6. Crea nueva Temporada activa
```

### 4. Generador de Eventos (`EventoComunidadServiceImpl`)

Métodos `@Async` que crean registros en `EventoComunidad` sin bloquear la transacción principal:

|         Evento         |                Cuándo se dispara                   |
|------------------------|----------------------------------------------------|
| `HABILIDAD_COMPLETADA` | Al completar todos los ejercicios de una habilidad |
| `NIVEL_ALCANZADO`      | Cuando el usuario sube de nivel                    |
| `LIGA_ASCENSO`         | Cuando el cron reasigna a una liga superior        |

---

## Seguridad

### Flujo de autenticación

```
1. POST /api/auth/register  →  crea usuario + retorna JWT
2. POST /api/auth/login     →  valida credenciales + retorna JWT
3. Resto de requests        →  Header: Authorization: Bearer <token>
```

### Rutas públicas (Modo Invitado)

Sin token — acceso de solo lectura al catálogo:

```
GET /api/actividades
GET /api/actividades/{id}
GET /api/actividades/{id}/secciones
GET /api/secciones/{id}/habilidades
GET /api/habilidades/{id}
```

### Rutas protegidas

Requieren `Authorization: Bearer <jwt>`:

```
GET/PUT /api/usuarios/me
GET     /api/usuarios/me/xp
POST    /api/progreso/completar
GET     /api/progreso/habilidades/**
GET     /api/clasificacion/**
GET     /api/comunidad/eventos
```

---

## Endpoints

### Auth

| Método |         Ruta         |        Body       |       Descripción         |
|--------|----------------------|-------------------|---------------------------|
| `POST` | `/api/auth/register` | `RegisterRequest` | Crea cuenta y retorna JWT |
| `POST` | `/api/auth/login`    | `LoginRequest`    | Autentica y retorna JWT   |

### Catálogo (público — Modo Invitado)

| Método |              Ruta                 |           Descripción          |
|--------|-----------------------------------|--------------------------------|
| `GET`  | `/api/actividades`                | Lista todas las actividades    |
| `GET`  | `/api/actividades/{id}`           | Detalle de actividad           |
| `GET`  | `/api/actividades/{id}/secciones` | Secciones ordenadas            |
| `GET`  | `/api/secciones/{id}/habilidades` | Habilidades con conteo         |
| `GET`  | `/api/habilidades/{id}`           | Habilidad + ejercicios + pasos |
x  
### Progreso de usuario (requiere JWT)

| Método |             Ruta                 |            Body             |                   Descripción                     |
|--------|----------------------------------|-----------------------------|---------------------------------------------------|
| `POST` | `/api/progreso/completar`        | `CompletarEjercicioRequest` | Completa ejercicio → dispara todos los motores    |
| `GET`  | `/api/progreso/habilidades`      |              —              | Lista progreso de todas las habilidades iniciadas |
| `GET`  | `/api/progreso/habilidades/{id}` |              —              | Progreso de una habilidad específica              |

### Perfil (requiere JWT)

| Método |        Ruta           |          Body             |                  Descripción                      |
|--------|-----------------------|---------------------------|---------------------------------------------------|
|  `GET` | `/api/usuarios/me`    |           —               | Perfil completo + racha + habilidades completadas |
|  `PUT` | `/api/usuarios/me`    | `ActualizarPerfilRequest` | Actualiza nombre y/o avatar                       |
|  `GET` | `/api/usuarios/me/xp` |           —               | Historial de transacciones de XP                  |

### Clasificación (requiere JWT)

| Método |            Ruta                  |     Params     |          Descripción             |
|--------|----------------------------------|----------------|----------------------------------|
|  `GET` | `/api/clasificacion`             | `page`, `size` | Ranking global paginado          |
|  `GET` | `/api/clasificacion/ligas`       |        —       | Lista de ligas con colores       |
|  `GET` | `/api/clasificacion/ligas/{id}`  | `page`, `size` | Ranking de una liga              |
|  `GET` | `/api/clasificacion/mi-posicion` |        —       | Posición y percentil del usuario |

### Comunidad (requiere JWT)

| Método |          Ruta            |      Params    |              Descripción                |
|--------|--------------------------|----------------|-----------------------------------------|
|  `GET` | `/api/comunidad/eventos` | `page`, `size` | Feed paginado de logros de la comunidad |

---

## Swagger UI

Con el servidor corriendo, accede a:

```
http://localhost:8080/swagger-ui.html
```

Para probar endpoints protegidos:
1. Ejecuta `POST /api/auth/register` o `POST /api/auth/login`.
2. Copia el valor del campo `token` de la respuesta.
3. Haz clic en el botón **Authorize** (candado) en la esquina superior derecha.
4. Pega el token y confirma.
5. Ahora todos los endpoints protegidos incluyen el header automáticamente.

---

## Variables de entorno / propiedades

Todas las propiedades configurables están en `src/main/resources/application.properties`:

|            Propiedad            |                     Default                    |                         Descripción                         |
|---------------------------------|------------------------------------------------|-------------------------------------------------------------|
| `spring.datasource.url`         | `jdbc:postgresql://localhost:5432/athletiq_db` | URL de conexión a PostgreSQL                                |
| `spring.datasource.username`    | `postgres`                                     | Usuario de BD                                               |
| `spring.datasource.password`    | *(vacío)*                                      | Contraseña de BD                                            |
| `spring.jpa.hibernate.ddl-auto` | `update`                                       | Estrategia de esquema (`update` en dev, `validate` en prod) |
| `athletiq.jwt.secret`           | *(placeholder)*                                | Secret Base64 para firmar JWT (mín. 32 chars)               |
| `athletiq.jwt.expiration`       | `86400000`                                     | Expiración del JWT en ms (24 h)                             |
| `athletiq.cors.allowed-origins` | `http://localhost:5173,...`                    | Orígenes permitidos para CORS                               |
| `springdoc.swagger-ui.path`     | `/swagger-ui.html`                             | Ruta de Swagger UI                                          |
| `server.port`                   | `8080`                                         | Puerto del servidor                                         |

---

## Estructura de carpetas

```
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/athletiq/backend/
    │   │   ├── AthletiqBackendApplication.java
    │   │   ├── config/
    │   │   ├── controllers/
    │   │   ├── dtos/
    │   │   │   ├── request/
    │   │   │   └── response/
    │   │   ├── exceptions/
    │   │   ├── models/
    │   │   │   ├── entities/
    │   │   │   ├── enums/
    │   │   │   └── keys/
    │   │   ├── repositories/
    │   │   ├── security/
    │   │   └── services/
    │   │       ├── impl/
    │   │       └── scheduled/
    │   └── resources/
    │       └── application.properties
    └── test/
        ├── java/com/athletiq/backend/
        │   ├── controllers/
        │   └── services/
        └── resources/
            └── application-test.properties
```
