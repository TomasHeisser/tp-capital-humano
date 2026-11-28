# Capit@l Humano

Sistema web de evaluación de candidatos por competencias para la consultora de RR.HH. Capit@l Humano.
Trabajo práctico de **Diseño de Sistemas** y **Desarrollo de Software** – UTN FRSF, 2026 – **Grupo 32**.

Integrantes: Tomás Heisser, Luciano Cano, Thiago Aguirre, Estanislao Gallo y Joaquín Collomb.

---

## Contenido del repositorio

| Carpeta | Qué hay |
| --- | --- |
| `api/` | Backend: Java 25 + Spring Boot (API REST, gestores, DAO, JPA, seguridad con JWT, LDAP embebido) |
| `frontend/` | Frontend: Next.js (App Router) + TypeScript + Tailwind CSS |
| `db/` | `schema.sql` (creación de tablas) y `seed.sql` (datos de prueba) |
| `docs/diagramas/` | Diagramas de clases, secuencia, entidad-relación y despliegue (SVG + código PlantUML / draw.io) |
| `docker-compose.yml` | Base PostgreSQL 16 para correr local |

Arquitectura: **Pantalla (Next.js) → Controller (REST) → Gestor (reglas de negocio) → DAO (EntityManager) → PostgreSQL**.
Los controllers solo traducen HTTP ↔ DTO; los gestores reciben y devuelven DTO y marcan las transacciones.

---

## Requisitos

- Java 25 (Temurin). No hace falta instalar Maven: se usa el wrapper (`mvnw` / `mvnw.cmd`).
- Node.js LTS (con npm).
- Docker Desktop (para la base). Sin Docker, PostgreSQL 16 instalado (ver más abajo).

---

## Cómo correrlo

### 1. Base de datos

Desde la raíz del repo:

```bash
docker compose up -d
```

Levanta PostgreSQL 16 en el puerto **5433** de la compu (base `capitalhumano`, usuario y contraseña `capitalhumano`, solo local)
y la primera vez corre `db/schema.sql` y `db/seed.sql`.

**Volver a cargar el seed** (por ejemplo antes de una demo, porque algunos cuestionarios vencen con el tiempo):

```bash
docker compose down -v
docker compose up -d
```

**Sin Docker:** crear la base y el usuario en un PostgreSQL 16 propio y correr los scripts en orden:

```bash
createuser -P capitalhumano          # contraseña: capitalhumano
createdb -O capitalhumano capitalhumano
psql -U capitalhumano -d capitalhumano -f db/schema.sql
psql -U capitalhumano -d capitalhumano -f db/seed.sql
```

Si el PostgreSQL propio usa el puerto 5432, se indica con la variable `DB_URL=jdbc:postgresql://localhost:5432/capitalhumano`.

### 2. Backend

```bash
cd api
cp .env.example .env          # en Windows: copy .env.example .env
./mvnw spring-boot:run        # en Windows: .\mvnw.cmd spring-boot:run
```

- API en `http://localhost:8080`.
- Swagger en `http://localhost:8080/swagger-ui.html`.
- El LDAP de la organización es un servidor embebido (puerto 8389) que se carga de `api/src/main/resources/directorio.ldif`.
- Variables de `api/.env` (el archivo no se sube al repo):
  - `GEMINI_API_KEY`: clave de Google Gemini para "Generar con IA". Si queda vacía se usa el asistente simulado.
  - `IA_PROVEEDOR=simulado`: fuerza el asistente simulado aunque haya clave.
  - `JWT_SECRET`: secreto para firmar los tokens; si queda vacío se usa uno de desarrollo. El token dura 8 horas.
- Los parámetros del cuestionario (instrucciones, 5 preguntas por bloque, 120 minutos de tiempo máximo, 7 días de vigencia
  y 5 accesos) están en `api/src/main/resources/application.properties`.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend en `http://localhost:3000`. La URL de la API está en `frontend/.env.development` (`NEXT_PUBLIC_API_URL`).

---

## Tests

```bash
cd api
./mvnw verify        # en Windows: .\mvnw.cmd verify
```

Corre los tests unitarios (JUnit 5 + Mockito, sin base ni LDAP) y controla con JaCoCo que `GestorCuestionario`,
`GestorPreguntas` y `GestorReportes` tengan **al menos 80% de cobertura de líneas** (si no, el build falla).
El reporte queda en `api/target/site/jacoco/index.html`.

La CI de GitHub Actions (`.github/workflows/ci.yml`) corre `./mvnw verify` y `npm run build` en cada pull request.

---

## Usuarios de prueba

Solo para correr local (vienen en el LDAP embebido y en el seed).

**Consultores** (Ingresar como usuario):

| Usuario | Contraseña |
| --- | --- |
| `JPEREZ` | `jperez2026` |
| `MGARCIA` | `mgarcia2026` |

**Candidatos** (Realizar cuestionario):

| Candidato | Documento | Clave | Estado del cuestionario |
| --- | --- | --- | --- |
| LÓPEZ, CAMILA | DNI 40111222 | `T6Q9D2LM` | Activo (DEV-JR-01) |
| DÍAZ, JULIETA | DNI 38222333 | `R8V3K5NW` | Activo (DEV-JR-01) |
| MARTÍNEZ, DIEGO | DNI 28765432 | `M4X7P2QA` | En proceso, bloque 1 respondido (AF-03) |
| BENÍTEZ, CARLA | DNI 37555666 | `B3N7C5KD` | Sin contestar: no puede ingresar |

El seed también trae cuestionarios completos (GÓMEZ, SÁNCHEZ, FERNÁNDEZ) e incompletos (RODRÍGUEZ) para los reportes, y
candidatos sin evaluaciones (ACOSTA, PEREYRA, ROMERO) para evaluar en CU025. El puesto QA-04 no tiene evaluaciones.

---

## Casos de uso de Diseño: dónde está cada uno

Los seis CU con diagrama de secuencia. Paquete base del backend: `ar.utn.frsf.capitalhumano`.

| CU | Pantalla (ruta del frontend) | Controller | Gestor y otras clases | Endpoints |
| --- | --- | --- | --- | --- |
| 020 Gestionar preguntas | `PantallaGestionarPreguntas` (`/preguntas`) | `controlador.PreguntaController`, `controlador.CompetenciaController` | `gestor.GestorPreguntas`, `gestor.GestorCompetencias`, `dao.PreguntaDAO` | `GET /api/preguntas`, `GET /api/competencias`, `GET /api/competencias/{id}/factores` |
| 021 Dar de alta pregunta (+ CU033) | `PantallaAltaPregunta` (`/preguntas/nueva`), `PantallaAsistenteIA` (diálogo) | `controlador.PreguntaController`, `controlador.OpcionRespuestaController` | `gestor.GestorPreguntas`, `integracion.IAsistenteIA` (`AdaptadorServicioIA`, `AsistenteIASimulado`) | `POST /api/preguntas`, `POST /api/preguntas/ia`, `GET /api/opciones-respuesta` |
| 023 Dar de baja pregunta | `PantallaBajaPregunta` (diálogo sobre `/preguntas`) | `controlador.PreguntaController` | `gestor.GestorPreguntas`, `gestor.GestorAuditoria` | `DELETE /api/preguntas/{id}` |
| 025 Evaluar candidatos | `PantallaEvaluarCandidatos` (`/evaluar-candidatos`) | `controlador.EvaluacionController`, `controlador.PuestoController` | `gestor.GestorEvaluaciones`, `gestor.GestorPuestos`, `exportacion.ExportadorExcel` | `GET /api/evaluaciones/candidatos`, `POST /api/evaluaciones/verificacion-activos`, `GET /api/puestos`, `GET /api/evaluaciones/puestos/{id}/validacion`, `POST /api/evaluaciones/claves`, `POST /api/evaluaciones`, `GET /api/evaluaciones/{id}/claves` |
| 026 Completar cuestionario | `PantallaCuestionario` (`/cuestionario`) | `controlador.CuestionarioController` | `gestor.GestorCuestionario`, `dominio.Cuestionario` + `EstadoCuestionario` (State), `FabricaEstadoCuestionario` | `POST /api/cuestionarios/{id}/ingreso`, `POST /api/cuestionarios/{id}/comienzo`, `POST /api/cuestionarios/{id}/bloques/{nro}` |
| 028 Emitir orden de mérito | `PantallaOrdenMerito` (`/orden-merito`) | `controlador.ReporteController` | `gestor.GestorReportes`, `reporte.ReporteOrdenMerito` (Strategy) | `GET /api/reportes/puestos`, `GET /api/reportes/puestos/{id}/evaluaciones`, `POST /api/reportes/ORDEN_MERITO` |

---

## Endpoints

Todas las rutas salvo el login piden el encabezado `Authorization: Bearer <token>`. Las de `/api/cuestionarios/{id}`
son solo del candidato dueño de ese cuestionario; el resto, del consultor. Los errores vuelven como
`{ "mensaje": "...", "errores": [...] }` con código 400 (validación), 404 (no existe), 409 (duplicado),
422 (regla de negocio) o 503 (servicio de IA).

| CU | Método y ruta | Entrada → salida |
| --- | --- | --- |
| 001 | `POST /api/auth/login` | `CredencialesDTO` → `SesionDTO` |
| 006 | `POST /api/candidatos/importacion` (multipart, campo `archivo`) | CSV → `ResultadoImportacionDTO` |
| 008 | `GET /api/puestos?codigo=&nombre=&empresa=` | → `PuestoDTO[]` |
| 009 | `GET /api/empresas` · `POST /api/puestos` | → `EmpresaDTO[]` · `PuestoAltaDTO` → `boolean` |
| 012 / 013 | `GET /api/competencias[?nombre=]` · `POST /api/competencias` | → `CompetenciaDTO[]` · `CompetenciaDTO` → `boolean` |
| 016 / 017 | `GET /api/factores?competencia=&nombre=` · `GET /api/competencias/{id}/factores` · `GET /api/competencias/{id}/ordenes-disponibles` · `POST /api/factores` | → `FactorDTO[]` · → `Integer[]` · `FactorDTO` → `boolean` |
| 020 | `GET /api/preguntas?idCompetencia=&idFactor=&nombre=` · `GET /api/preguntas/{id}` | → `PreguntaResumenDTO[]` · → `PreguntaDTO` |
| 021 | `POST /api/preguntas` | `PreguntaDTO` → `boolean` |
| 022 | `PUT /api/preguntas/{id}` | `PreguntaDTO` → `boolean` |
| 023 | `DELETE /api/preguntas/{id}` | → `boolean` |
| 033 | `POST /api/preguntas/ia` | `SolicitudIADTO` → `PreguntaDTO` |
| 024 / 029 | `GET /api/opciones-respuesta[?nombre=]` · `GET /api/opciones-respuesta/{id}` · `POST /api/opciones-respuesta` | → `OpcionRespuestaDTO[]` · → `OpcionRespuestaDTO` · `OpcionRespuestaDTO` → `boolean` |
| 025 | `GET /api/evaluaciones/candidatos?apellido=&nombre=&nroCandidato=` · `POST /api/evaluaciones/verificacion-activos` · `GET /api/evaluaciones/puestos/{id}/validacion` · `POST /api/evaluaciones/claves` · `POST /api/evaluaciones` · `GET /api/evaluaciones/{id}/claves` | → `CandidatoGestionDTO[]` · `Long[]` → `CandidatoGestionDTO[]` · → `CompetenciaRequeridaDTO[]` · `Long[]` → `CandidatoClaveDTO[]` · `NuevaEvaluacionDTO` → `EvaluacionDTO` · → archivo `.xlsx` |
| 026 | `POST /api/cuestionarios/{id}/ingreso` · `POST /api/cuestionarios/{id}/comienzo` · `POST /api/cuestionarios/{id}/bloques/{nro}` | → `CuestionarioDTO` · → `BloqueDTO` · `RespuestaDTO[]` → `CuestionarioDTO` |
| 027 / 028 | `GET /api/reportes/puestos?codigo=&nombre=&empresa=` · `GET /api/reportes/puestos/{id}/evaluaciones` · `GET /api/reportes/puestos/{id}/evaluados` · `POST /api/reportes/{tipo}` (`ORDEN_MERITO` o `COMPARATIVO`) | → `PuestoEstadisticaDTO[]` · → `EvaluacionDTO[]` · → `CandidatoEvaluadoDTO[]` · `CriterioReporteDTO` → `ReporteDTO` |
| 032 | Sin endpoint: `TareaFinalizarCuestionarios` corre sola cada 5 minutos | |

En `GET /api/evaluaciones/{id}/claves`, `GestorEvaluaciones.exportarClaves` devuelve un `ArchivoDTO` (nombre y contenido) y `EvaluacionController` lo manda como descarga del `.xlsx`.

### Ejemplos con curl

```bash
# CU001: login del consultor (guardar el token)
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"tipoAcceso":"CONSULTOR","usuario":"JPEREZ","contrasenia":"jperez2026"}'
TOKEN=<token devuelto>

# CU001: login del candidato (devuelve idCuestionario)
curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"tipoAcceso":"CANDIDATO","tipoDocumento":"DNI","nroDocumento":"40111222","clave":"T6Q9D2LM"}'

# CU020: buscar preguntas de un factor que contengan "WBS"
curl -s "http://localhost:8080/api/preguntas?idFactor=1&nombre=wbs" -H "Authorization: Bearer $TOKEN"

# CU021: alta de pregunta (las ponderaciones suman 10)
curl -s -X POST http://localhost:8080/api/preguntas -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"idCompetencia":1,"idFactor":1,"nombre":"ALCANCE Y CAMBIOS","enunciado":"¿TODO CAMBIO DE ALCANCE DEBE PASAR POR CONTROL DE CAMBIOS?","descripcion":null,"idOpcionRespuesta":1,"ponderaciones":[{"idItem":1,"ponderacion":10},{"idItem":2,"ponderacion":0}]}'

# CU023: baja de pregunta (queda registrada en la auditoría)
curl -s -X DELETE http://localhost:8080/api/preguntas/1 -H "Authorization: Bearer $TOKEN"

# CU025: candidatos que se llamen "acosta" y validar el puesto 4
curl -s "http://localhost:8080/api/evaluaciones/candidatos?apellido=acosta" -H "Authorization: Bearer $TOKEN"
curl -s "http://localhost:8080/api/evaluaciones/puestos/4/validacion" -H "Authorization: Bearer $TOKEN"

# CU026: ingreso del candidato a su cuestionario (con el token del candidato)
curl -s -X POST http://localhost:8080/api/cuestionarios/<idCuestionario>/ingreso -H "Authorization: Bearer $TOKEN_CANDIDATO"

# CU028: orden de mérito de todas las evaluaciones del puesto 1
curl -s -X POST http://localhost:8080/api/reportes/ORDEN_MERITO -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"idPuesto":1,"idsEvaluaciones":[1,2],"idCuestionario":null}'
```

En Swagger (`/swagger-ui.html`) están todos los endpoints con sus DTO; con el botón **Authorize** se pega el token.

---

## Patrones de diseño

| Patrón | Dónde |
| --- | --- |
| DAO | `dao.DAO<T, ID>`, una interfaz por entidad y su implementación en `dao.jpa` (con `EntityManager`) |
| Adapter | `autenticacion.AdaptadorLDAP` (adapta `LdapTemplate` a `IServicioDirectorio`); `integracion.AdaptadorServicioIA` (adapta la API de Gemini a `IAsistenteIA`) |
| Strategy | `GestorAutenticacion` elige `AutenticacionConsultor` o `AutenticacionCandidato`; `GestorReportes` elige `ReporteOrdenMerito` o `ReporteComparativo` |
| State | `dominio.Cuestionario` delega en `EstadoCuestionario` (`EstadoActivo`, `EstadoEnProceso`, `EstadoCompleto`, `EstadoIncompleto`, `EstadoSinContestar`) |
| Factory Method | `dominio.FabricaEstadoCuestionario.crearEstado(nombre)`, usada por `ConversorEstadoCuestionario` al leer de la base y en cada cambio de estado |

---

## Diagramas

En `docs/diagramas/`: clases, secuencia de CU020, CU021, CU023, CU025, CU026 y CU028, entidad-relación y despliegue,
cada uno en SVG y con su código fuente.

---

## Forma de trabajo

- Ramas: `main` (entregas), `develop` (integración) y `feature/<tema>`. Todo entra por pull request con una aprobación.
- Commits con Conventional Commits en español (`feat(cu020): ...`, `fix: ...`, `test: ...`, `docs: ...`).
- Etiquetas: `v1.0-disenio` (entrega de Diseño de Sistemas) y `v2.0` (entrega de Desarrollo de Software).
