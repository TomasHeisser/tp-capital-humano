@startuml
skinparam classAttributeIconSize 0
skinparam linetype ortho
skinparam nodesep 45
skinparam ranksep 60
skinparam shadowing false
skinparam packageStyle rectangle
skinparam defaultFontSize 12
skinparam class {
  BackgroundColor White
  BorderColor #333333
}
hide empty members

title Diagrama de Clases de Diseño (versión final) - Sistema Capit@l Humano - Grupo 32

' ==========================================================
' 1. PRESENTACIÓN: cada «boundary» es una página Next.js
' ==========================================================
package "presentacion (Next.js)" as P_UI #E6F7FF {
  class PantallaLogin <<boundary>>
  class PantallaPrincipal <<boundary>>
  class PantallaImportarCandidatos <<boundary>>
  class PantallaGestionarPuestos <<boundary>>
  class PantallaAltaPuesto <<boundary>>
  class PantallaGestionarCompetencias <<boundary>>
  class PantallaAltaCompetencia <<boundary>>
  class PantallaGestionarFactores <<boundary>>
  class PantallaAltaFactor <<boundary>>
  class PantallaGestionarPreguntas <<boundary>> {
    + actualizarGrilla(): void
  }
  class PantallaAltaPregunta <<boundary>>
  class PantallaModificarPregunta <<boundary>>
  class PantallaBajaPregunta <<boundary>>
  class PantallaAsistenteIA <<boundary>>
  class PantallaGestionarOpcionesRespuesta <<boundary>>
  class PantallaAltaOpcionRespuesta <<boundary>>
  class PantallaEvaluarCandidatos <<boundary>>
  class PantallaCuestionario <<boundary>>
  class PantallaReporteComparativo <<boundary>>
  class PantallaOrdenMerito <<boundary>> {
    + imprimir(): void
  }
}

' ==========================================================
' 2. CONTROLADORES REST (Spring Boot): solo reciben y devuelven DTO
' ==========================================================
package "controlador (API REST)" as P_CTRL #FFF5E6 {
  class AutenticacionController <<control>> {
    + login(cred: CredencialesDTO): SesionDTO
  }
  class CandidatoController <<control>> {
    + importar(archivo: MultipartFile): ResultadoImportacionDTO
  }
  class PuestoController <<control>> {
    + buscar(filtro: FiltroPuestoDTO): List<PuestoDTO>
    + listarEmpresas(): List<EmpresaDTO>
    + registrar(p: PuestoAltaDTO): boolean
  }
  class CompetenciaController <<control>> {
    + buscarCompetencias(nombre: String): List<CompetenciaDTO>
    + listarCompetencias(): List<CompetenciaDTO>
    + registrarCompetencia(c: CompetenciaDTO): boolean
    + buscarFactores(nombreComp: String, nombreFactor: String): List<FactorDTO>
    + listarFactores(idCompetencia: Long): List<FactorDTO>
    + ordenesDisponibles(idCompetencia: Long): List<Integer>
    + registrarFactor(f: FactorDTO): boolean
  }
  class PreguntaController <<control>> {
    + buscar(filtro: FiltroPreguntaDTO): List<PreguntaResumenDTO>
    + obtener(id: Long): PreguntaDTO
    + registrar(p: PreguntaDTO): boolean
    + modificar(p: PreguntaDTO): boolean
    + darDeBaja(id: Long): boolean
    + generarConIA(s: SolicitudIADTO): PreguntaDTO
  }
  class OpcionRespuestaController <<control>> {
    + buscar(nombre: String): List<OpcionRespuestaDTO>
    + listarOpciones(): List<OpcionRespuestaDTO>
    + obtener(id: Long): OpcionRespuestaDTO
    + registrar(o: OpcionRespuestaDTO): boolean
  }
  class EvaluacionController <<control>> {
    + buscarCandidatos(f: FiltroCandidatoDTO): List<CandidatoGestionDTO>
    + verificarActivos(ids: List<Long>): List<CandidatoGestionDTO>
    + validarPuesto(idPuesto: Long): List<CompetenciaRequeridaDTO>
    + generarClaves(ids: List<Long>): List<CandidatoClaveDTO>
    + registrar(e: NuevaEvaluacionDTO): EvaluacionDTO
    + exportarClaves(idEvaluacion: Long): ArchivoDTO
  }
  class CuestionarioController <<control>> {
    + ingresar(idCuestionario: Long): CuestionarioDTO
    + comenzar(idCuestionario: Long): BloqueDTO
    + responderBloque(idCuestionario: Long, nroBloque: Integer, r: List<RespuestaDTO>): CuestionarioDTO
  }
  class ReporteController <<control>> {
    + buscarPuestos(f: FiltroPuestoDTO): List<PuestoEstadisticaDTO>
    + listarEvaluaciones(idPuesto: Long): List<EvaluacionDTO>
    + listarEvaluados(idPuesto: Long): List<CandidatoEvaluadoDTO>
    + emitir(tipo: TipoReporte, criterio: CriterioReporteDTO): ReporteDTO
  }
}

' ==========================================================
' 3. DTO: solo datos, sin lógica (regla de oro de la cátedra)
' ==========================================================
package "dto" as P_DTO #F5EEFF {
  class ErrorDTO <<DTO>> {
    + mensaje: String
    + errores: List<String>
  }
  class CredencialesDTO <<DTO>> {
    + tipoAcceso: Rol
    + usuario: String
    + contrasenia: String
    + tipoDocumento: TipoDocumento
    + nroDocumento: String
    + clave: String
  }
  class SesionDTO <<DTO>> {
    + token: String
    + rol: Rol
    + nombreUsuario: String
    + idCuestionario: Long
  }
  class ResultadoImportacionDTO <<DTO>> {
    + creados: int
    + actualizados: int
    + eliminados: int
    + filasConError: List<String>
  }
  class EmpresaDTO <<DTO>> {
    + id: Long
    + nombre: String
  }
  class FiltroPuestoDTO <<DTO>> {
    + codigo: String
    + nombre: String
    + empresa: String
  }
  class PuestoDTO <<DTO>> {
    + id: Long
    + codigo: String
    + nombre: String
    + empresa: String
  }
  class PuestoAltaDTO <<DTO>> {
    + codigo: String
    + nombre: String
    + descripcion: String
    + idEmpresa: Long
    + competencias: List<CompetenciaRequeridaDTO>
  }
  class CompetenciaRequeridaDTO <<DTO>> {
    + idCompetencia: Long
    + nombreCompetencia: String
    + ponderacion: BigDecimal
  }
  class CompetenciaDTO <<DTO>> {
    + id: Long
    + codigo: String
    + nombre: String
    + descripcion: String
  }
  class FactorDTO <<DTO>> {
    + id: Long
    + codigo: String
    + nombre: String
    + descripcion: String
    + nroOrden: Integer
    + idCompetencia: Long
    + nombreCompetencia: String
  }
  class FiltroPreguntaDTO <<DTO>> {
    + idCompetencia: Long
    + idFactor: Long
    + nombre: String
  }
  class PreguntaResumenDTO <<DTO>> {
    + id: Long
    + competencia: String
    + factor: String
    + nombre: String
    + enunciado: String
  }
  class PreguntaDTO <<DTO>> {
    + id: Long
    + idCompetencia: Long
    + idFactor: Long
    + nombre: String
    + enunciado: String
    + descripcion: String
    + idOpcionRespuesta: Long
    + ponderaciones: List<PonderacionDTO>
  }
  class PonderacionDTO <<DTO>> {
    + idItem: Long
    + orden: Integer
    + descripcion: String
    + ponderacion: BigDecimal
  }
  class SolicitudIADTO <<DTO>> {
    + idCompetencia: Long
    + idFactor: Long
  }
  class OpcionRespuestaDTO <<DTO>> {
    + id: Long
    + nombre: String
    + descripcion: String
    + items: List<ItemOpcionDTO>
  }
  class ItemOpcionDTO <<DTO>> {
    + id: Long
    + orden: Integer
    + descripcion: String
  }
  class FiltroCandidatoDTO <<DTO>> {
    + apellido: String
    + nombre: String
    + nroCandidato: Long
  }
  class CandidatoGestionDTO <<DTO>> {
    + id: Long
    + nroCandidato: Long
    + apellido: String
    + nombre: String
    + tipoDocumento: TipoDocumento
    + nroDocumento: String
  }
  class CandidatoClaveDTO <<DTO>> {
    + idCandidato: Long
    + apellido: String
    + nombre: String
    + tipoDocumento: TipoDocumento
    + nroDocumento: String
    + clave: String
  }
  class NuevaEvaluacionDTO <<DTO>> {
    + idPuesto: Long
    + candidatos: List<CandidatoClaveDTO>
  }
  class EvaluacionDTO <<DTO>> {
    + id: Long
    + fechaCreacion: LocalDateTime
    + nombrePuesto: String
    + empresa: String
    + cantidadCandidatos: int
  }
  class ArchivoDTO <<DTO>> {
    + nombre: String
    + contenido: byte[]
  }
  class CuestionarioDTO <<DTO>> {
    + id: Long
    + estado: String
    + mensaje: String
    + instrucciones: String
    + nombrePuesto: String
    + bloqueActual: BloqueDTO
  }
  class BloqueDTO <<DTO>> {
    + nroBloque: Integer
    + totalBloques: Integer
    + esUltimo: boolean
    + preguntas: List<PreguntaCuestionarioDTO>
  }
  class PreguntaCuestionarioDTO <<DTO>> {
    + idPregunta: Long
    + enunciado: String
    + opciones: List<ItemOpcionDTO>
  }
  class RespuestaDTO <<DTO>> {
    + idPregunta: Long
    + idOpcion: Long
  }
  class PuestoEstadisticaDTO <<DTO>> {
    + id: Long
    + codigo: String
    + nombre: String
    + empresa: String
    + cantidadCandidatos: long
    + evaluacionesCompletas: long
  }
  class CriterioReporteDTO <<DTO>> {
    + idPuesto: Long
    + idsEvaluaciones: List<Long>
    + idCuestionario: Long
  }
  abstract class ReporteDTO <<DTO>> {
    + codigoPuesto: String
    + nombrePuesto: String
    + empresa: String
    + fechaEmision: LocalDateTime
  }
  class OrdenMeritoDTO <<DTO>> {
    + ordenMerito: List<FilaOrdenMeritoDTO>
    + noAlcanzaronMinimo: List<FilaOrdenMeritoDTO>
    + restoPorEstado: Map<String, List<FilaOrdenMeritoDTO>>
  }
  class FilaOrdenMeritoDTO <<DTO>> {
    + posicion: Integer
    + tipoDocumento: TipoDocumento
    + nroDocumento: String
    + apellido: String
    + nombre: String
    + estado: String
    + puntaje: BigDecimal
    + fechaInicio: LocalDateTime
    + fechaFin: LocalDateTime
    + ultimoIngreso: LocalDateTime
    + cantidadAccesos: Integer
  }
  class CandidatoEvaluadoDTO <<DTO>> {
    + idCuestionario: Long
    + apellido: String
    + nombre: String
    + tipoDocumento: TipoDocumento
    + nroDocumento: String
    + nroCandidato: Long
    + fechaFin: LocalDateTime
  }
  class ReporteComparativoDTO <<DTO>> {
    + candidato: String
    + filas: List<FilaComparativoDTO>
  }
  class FilaComparativoDTO <<DTO>> {
    + competencia: String
    + requerido: BigDecimal
    + obtenido: BigDecimal
  }
}

' ==========================================================
' 4. GESTORES: aplican las reglas de negocio y coordinan (cátedra)
' ==========================================================
package "gestor (lógica de negocio)" as P_GES #E9FFE9 {
  class GestorAutenticacion <<control>> {
    - estrategias: Map<Rol, EstrategiaAutenticacion>
    + autenticar(cred: CredencialesDTO): SesionDTO
  }
  class GestorImportacionCandidatos <<control>> {
    + importar(csv: InputStream): ResultadoImportacionDTO
    - procesarFila(columnas: String[]): Candidato
  }
  class GestorPuestos <<control>> {
    + buscarPuestos(filtro: FiltroPuestoDTO): List<PuestoDTO>
    + listarEmpresas(): List<EmpresaDTO>
    + registrarPuesto(datos: PuestoAltaDTO): boolean
    - validar(datos: PuestoAltaDTO): List<String>
  }
  class GestorCompetencias <<control>> {
    + buscarCompetencias(nombre: String): List<CompetenciaDTO>
    + listarCompetencias(): List<CompetenciaDTO>
    + registrarCompetencia(datos: CompetenciaDTO): boolean
    + buscarFactores(nombreComp: String, nombreFactor: String): List<FactorDTO>
    + listarFactores(idCompetencia: Long): List<FactorDTO>
    + obtenerOrdenesDisponibles(idCompetencia: Long): List<Integer>
    + registrarFactor(datos: FactorDTO): boolean
  }
  class GestorPreguntas <<control>> {
    + buscarPreguntas(filtro: FiltroPreguntaDTO): List<PreguntaResumenDTO>
    + obtenerPregunta(id: Long): PreguntaDTO
    + registrarPregunta(datos: PreguntaDTO): boolean
    + modificarPregunta(datos: PreguntaDTO): boolean
    + darDeBajaPregunta(id: Long, username: String): boolean
    + generarPreguntaIA(s: SolicitudIADTO): PreguntaDTO
    - validar(datos: PreguntaDTO): List<String>
  }
  class GestorOpcionesRespuesta <<control>> {
    + buscarOpciones(nombre: String): List<OpcionRespuestaDTO>
    + listarOpciones(): List<OpcionRespuestaDTO>
    + obtenerOpcion(id: Long): OpcionRespuestaDTO
    + registrarOpcion(datos: OpcionRespuestaDTO): boolean
    - validar(datos: OpcionRespuestaDTO): List<String>
  }
  class GestorAuditoria <<control>> {
    + registrarBaja(accion: String, entidad: String, idObjeto: Long, c: Consultor): boolean
  }
  class GestorEvaluaciones <<control>> {
    + buscarCandidatos(f: FiltroCandidatoDTO): List<CandidatoGestionDTO>
    + verificarCuestionariosActivos(ids: List<Long>): List<CandidatoGestionDTO>
    + validarPuesto(idPuesto: Long): List<CompetenciaRequeridaDTO>
    + generarClaves(ids: List<Long>): List<CandidatoClaveDTO>
    + registrarEvaluacion(datos: NuevaEvaluacionDTO, username: String): EvaluacionDTO
    + exportarClaves(idEvaluacion: Long): ArchivoDTO
    - copiarModelo(p: Puesto): List<CompetenciaEvaluada>
    - generarClave(): String
  }
  class GestorCuestionario <<control>> {
    + ingresar(idCuestionario: Long): CuestionarioDTO
    + comenzar(idCuestionario: Long): BloqueDTO
    + responderBloque(idCuestionario: Long, nroBloque: Integer, r: List<RespuestaDTO>): CuestionarioDTO
    + finalizarVencidos(): int
    - sortearPreguntas(e: Evaluacion): List<PreguntaEvaluada>
    - armarBloques(preguntas: List<PreguntaEvaluada>, tam: Integer): List<BloqueCuestionario>
    - calcularResultados(c: Cuestionario): List<Resultado>
  }
  class GestorReportes <<control>> {
    - estrategias: Map<TipoReporte, EstrategiaReporte>
    + buscarPuestos(f: FiltroPuestoDTO): List<PuestoEstadisticaDTO>
    + listarEvaluaciones(idPuesto: Long): List<EvaluacionDTO>
    + listarEvaluados(idPuesto: Long): List<CandidatoEvaluadoDTO>
    + emitirReporte(tipo: TipoReporte, criterio: CriterioReporteDTO): ReporteDTO
  }
}

' ==========================================================
' 5. PATRONES: Strategy (autenticación y reportes), Adapter (IA, LDAP)
' ==========================================================
package "autenticacion (Strategy + Adapter)" as P_AUT #FFFDE6 {
  interface EstrategiaAutenticacion <<interface>> {
    + autenticar(cred: CredencialesDTO): SesionDTO
  }
  class AutenticacionConsultor <<Strategy>> {
    + autenticar(cred: CredencialesDTO): SesionDTO
  }
  class AutenticacionCandidato <<Strategy>> {
    + autenticar(cred: CredencialesDTO): SesionDTO
  }
  class ServicioToken {
    + generarToken(usuario: String, rol: Rol): String
    + validarToken(token: String): boolean
  }
  interface IServicioDirectorio <<interface>> {
    + validarCredenciales(usuario: String, contrasenia: String): boolean
  }
  class AdaptadorLDAP <<Adapter>> {
    - ldap: LdapTemplate
    + validarCredenciales(usuario: String, contrasenia: String): boolean
  }
}

package "integracion IA (Adapter)" as P_IA #FFFDE6 {
  interface IAsistenteIA <<interface>> {
    + generarPregunta(comp: Competencia, factor: Factor, opciones: List<OpcionRespuesta>): PreguntaDTO
  }
  class AdaptadorServicioIA <<Adapter>> {
    - api: APIGemini
    + generarPregunta(comp: Competencia, factor: Factor, opciones: List<OpcionRespuesta>): PreguntaDTO
    - armarPrompt(...): String
  }
  class AsistenteIASimulado {
    + generarPregunta(comp: Competencia, factor: Factor, opciones: List<OpcionRespuesta>): PreguntaDTO
  }
  class APIGemini <<Adaptee>> {
    + generateContent(prompt: String): String
  }
}

package "reporte (Strategy) y exportacion" as P_REP #FFF0E0 {
  interface EstrategiaReporte <<interface>> {
    + generar(criterio: CriterioReporteDTO): ReporteDTO
  }
  class ReporteOrdenMerito <<Strategy>> {
    + generar(criterio: CriterioReporteDTO): ReporteDTO
    - alcanzoMinimos(c: Cuestionario): boolean
    - puntajeTotal(c: Cuestionario): BigDecimal
    - ordenar(filas: List<FilaOrdenMeritoDTO>): void
  }
  class ReporteComparativo <<Strategy>> {
    + generar(criterio: CriterioReporteDTO): ReporteDTO
  }
  interface ExportadorClaves <<interface>> {
    + exportar(puesto: String, fecha: LocalDateTime, claves: List<CandidatoClaveDTO>): ArchivoDTO
  }
  class ExportadorExcel {
    + exportar(puesto: String, fecha: LocalDateTime, claves: List<CandidatoClaveDTO>): ArchivoDTO
    - nombreArchivo(puesto: String, fecha: LocalDateTime): String
  }
}

package "configuracion y tareas" as P_CFG #EEF2F7 {
  interface RepositorioConfiguracion <<interface>> {
    + getInstrucciones(): String
    + getPreguntasPorBloque(): Integer
    + getTiempoMaximoMinutos(): Integer
    + getVigenciaDias(): Integer
    + getMaxAccesos(): Integer
  }
  class RepositorioConfiguracionProperties {
    - archivo: application.properties
  }
  class TareaFinalizarCuestionarios <<scheduled>> {
    + ejecutar(): void
  }
}

package "excepcion" as P_EXC #F2F2F2 {
  class NegocioException {
    - errores: List<String>
  }
  class ValidacionException
  class PreguntaDuplicadaException
  class CompetenciasNoEvaluablesException
  class RespuestasIncompletasException
  class ServicioIAException
  class ManejadorErrores <<ControllerAdvice>> {
    + manejar(e: NegocioException): ErrorDTO
  }
}

' ==========================================================
' 6. DAO: interfaz genérica + interfaz por entidad + implementación JPA
' ==========================================================
package "dao (interfaces)" as P_DAO #FFE9E9 {
  interface "DAO<T, ID>" as DAO <<interface>> {
    + buscarPorId(id: ID): Optional<T>
    + buscarTodos(): List<T>
    + guardar(e: T): boolean
    + actualizar(e: T): boolean
  }
  interface ConsultorDAO <<interface>> {
    + buscarPorUsername(u: String): Optional<Consultor>
  }
  interface CandidatoDAO <<interface>> {
    + buscarPorNroCandidato(nro: Long): Optional<Candidato>
    + buscarPorDocumento(tipo: TipoDocumento, nro: String): Optional<Candidato>
    + buscarPorFiltros(apellido: String, nombre: String, nro: Long): List<Candidato>
  }
  interface EmpresaDAO <<interface>>
  interface PuestoDAO <<interface>> {
    + buscarPorFiltros(codigo: String, nombre: String, empresa: String): List<Puesto>
    + existeCodigo(codigo: String): boolean
    + existeNombre(nombre: String): boolean
  }
  interface CompetenciaDAO <<interface>> {
    + buscarPorNombre(nombre: String): List<Competencia>
    + existeCodigo(codigo: String): boolean
    + existeNombre(nombre: String): boolean
  }
  interface FactorDAO <<interface>> {
    + buscarPorFiltros(nombreComp: String, nombreFactor: String): List<Factor>
    + buscarPorCompetencia(idCompetencia: Long): List<Factor>
    + existeCodigo(codigo: String): boolean
    + existeNombreEnCompetencia(nombre: String, idCompetencia: Long): boolean
  }
  interface PreguntaDAO <<interface>> {
    + buscarPorFiltros(idComp: Long, idFactor: Long, nombre: String): List<Pregunta>
    + buscarCoincidencias(idFactor: Long, nombre: String, enunciado: String, idExcluir: Long): List<Pregunta>
  }
  interface OpcionRespuestaDAO <<interface>> {
    + buscarPorNombre(nombre: String): List<OpcionRespuesta>
    + existeNombre(nombre: String): boolean
  }
  interface EvaluacionDAO <<interface>> {
    + buscarPorPuesto(idPuesto: Long): List<Evaluacion>
  }
  interface CuestionarioDAO <<interface>> {
    + buscarActivoOEnProceso(idCandidato: Long): Optional<Cuestionario>
    + buscarPorEvaluaciones(ids: List<Long>): List<Cuestionario>
    + buscarCompletosPorPuesto(idPuesto: Long): List<Cuestionario>
    + buscarPendientes(): List<Cuestionario>
    + contarCandidatosPorPuesto(idPuesto: Long): long
    + contarCompletosPorPuesto(idPuesto: Long): long
  }
  interface RegistroAuditoriaDAO <<interface>>
  interface RegistroEjecucionDAO <<interface>>
}

package "dao.jpa (implementaciones)" as P_JPA #FFE9E9 {
  abstract class "DAOJpa<T, ID>" as DAOJpa {
    # em: EntityManager
    + buscarPorId(id: ID): Optional<T>
    + buscarTodos(): List<T>
    + guardar(e: T): boolean
    + actualizar(e: T): boolean
  }
  class ConsultorDAOJpa
  class CandidatoDAOJpa
  class EmpresaDAOJpa
  class PuestoDAOJpa
  class CompetenciaDAOJpa
  class FactorDAOJpa
  class PreguntaDAOJpa
  class OpcionRespuestaDAOJpa
  class EvaluacionDAOJpa
  class CuestionarioDAOJpa
  class RegistroAuditoriaDAOJpa
  class RegistroEjecucionDAOJpa
}

' ==========================================================
' 7. DOMINIO
' ==========================================================
package "dominio" as P_DOM #FFFFFF {
  abstract class Usuario <<entity>> {
    - id: Long
    + {abstract} getRol(): Rol
  }
  class Consultor <<entity>> {
    - username: String
    + getRol(): Rol
  }
  class Candidato <<entity>> {
    - nroCandidato: Long
    - apellido: String
    - nombre: String
    - tipoDocumento: TipoDocumento
    - nroDocumento: String
    - fechaNacimiento: LocalDate
    - genero: Genero
    - nacionalidad: String
    - email: String
    - escolaridad: String
    - fechaBaja: LocalDateTime
    + getRol(): Rol
  }
  class Empresa <<entity>> {
    - id: Long
    - nombre: String
  }
  class Puesto <<entity>> {
    - id: Long
    - codigo: String
    - nombre: String
    - descripcion: String
    - fechaBaja: LocalDateTime
  }
  class CompetenciaRequerida <<entity>> {
    - ponderacion: BigDecimal
  }
  class Competencia <<entity>> {
    - id: Long
    - codigo: String
    - nombre: String
    - descripcion: String
    - fechaBaja: LocalDateTime
  }
  class Factor <<entity>> {
    - id: Long
    - codigo: String
    - nombre: String
    - descripcion: String
    - nroOrden: Integer
    - fechaBaja: LocalDateTime
    + getPreguntasActivas(): List<Pregunta>
  }
  class Pregunta <<entity>> {
    - id: Long
    - nombre: String
    - enunciado: String
    - descripcion: String
    - fechaBaja: LocalDateTime
    + agregarPonderacion(item: ItemOpcionRespuesta, valor: BigDecimal): void
    + darDeBaja(fecha: LocalDateTime): void
  }
  class OpcionRespuesta <<entity>> {
    - id: Long
    - nombre: String
    - descripcion: String
    - fechaBaja: LocalDateTime
    + getItem(idItem: Long): ItemOpcionRespuesta
  }
  class ItemOpcionRespuesta <<entity>> {
    - id: Long
    - orden: Integer
    - descripcion: String
  }
  class PonderacionPreguntaOpcion <<entity>> {
    - ponderacion: BigDecimal
  }

  class Evaluacion <<entity>> {
    - id: Long
    - fechaCreacion: LocalDateTime
    + agregarCompetenciaEvaluada(ce: CompetenciaEvaluada): void
    + agregarCuestionario(c: Cuestionario): void
  }
  class CompetenciaEvaluada <<entity>> {
    - id: Long
    - codigo: String
    - nombre: String
    - ponderacionMinima: BigDecimal
    + agregarFactor(fe: FactorEvaluado): void
  }
  class FactorEvaluado <<entity>> {
    - id: Long
    - codigo: String
    - nombre: String
    - nroOrden: Integer
    + FactorEvaluado(origen: Factor)
    + agregarPregunta(pe: PreguntaEvaluada): void
  }
  class PreguntaEvaluada <<entity>> {
    - id: Long
    - nombre: String
    - enunciado: String
    + PreguntaEvaluada(origen: Pregunta)
  }
  class OpcionEvaluada <<entity>> {
    - id: Long
    - orden: Integer
    - descripcion: String
    - ponderacion: BigDecimal
  }

  class Cuestionario <<entity>> {
    - id: Long
    - clave: String
    - fechaCreacion: LocalDateTime
    - fechaInicio: LocalDateTime
    - fechaFin: LocalDateTime
    - ultimoIngreso: LocalDateTime
    - fechaCambioEstado: LocalDateTime
    - cantidadAccesos: Integer
    - preguntasPorBloque: Integer
    - tiempoMaximoMinutos: Integer
    - vigenciaDias: Integer
    - maxAccesos: Integer
    + registrarAcceso(ahora: LocalDateTime): boolean
    + iniciar(bloques: List<BloqueCuestionario>, ahora: LocalDateTime): void
    + completar(resultados: List<Resultado>, ahora: LocalDateTime): void
    + verificarVencimiento(ahora: LocalDateTime): boolean
    + cambiarEstado(nombreEstado: String, ahora: LocalDateTime): void
    + getBloquePendiente(): BloqueCuestionario
  }
  abstract class EstadoCuestionario <<State>> {
    + registrarAcceso(c: Cuestionario, ahora: LocalDateTime): boolean
    + iniciar(c: Cuestionario, ahora: LocalDateTime): void
    + completar(c: Cuestionario, ahora: LocalDateTime): void
    + verificarVencimiento(c: Cuestionario, ahora: LocalDateTime): boolean
    + {abstract} getNombre(): String
  }
  class EstadoActivo <<State>>
  class EstadoEnProceso <<State>>
  class EstadoCompleto <<State>>
  class EstadoIncompleto <<State>>
  class EstadoSinContestar <<State>>
  class FabricaEstadoCuestionario <<Factory>> {
    + crearEstado(nombre: String): EstadoCuestionario
  }
  class ConversorEstadoCuestionario <<AttributeConverter>> {
    + aColumna(e: EstadoCuestionario): String
    + aEntidad(nombre: String): EstadoCuestionario
  }

  class BloqueCuestionario <<entity>> {
    - nroBloque: Integer
    - respondido: Boolean
    + registrarRespuesta(p: PreguntaEvaluada, o: OpcionEvaluada, ahora: LocalDateTime): void
    + estaCompleto(): boolean
  }
  class RespuestaCandidato <<entity>> {
    - fechaRespuesta: LocalDateTime
  }
  class Resultado <<entity>> {
    - puntaje: BigDecimal
  }
  class ResultadoFactor <<entity>> {
    - puntaje: BigDecimal
  }
  class RegistroAuditoria <<entity>> {
    - id: Long
    - fechaHora: LocalDateTime
    - accion: String
    - entidadAfectada: String
    - idObjetoAfectado: Long
  }
  class RegistroEjecucion <<entity>> {
    - id: Long
    - fechaHora: LocalDateTime
    - accion: AccionEjecucion
  }

  enum Rol {
    CONSULTOR
    CANDIDATO
  }
  enum TipoDocumento {
    DNI
    LE
    LC
    PP
  }
  enum Genero {
    H
    M
  }
  enum TipoReporte {
    ORDEN_MERITO
    COMPARATIVO
  }
  enum AccionEjecucion {
    INICIO
    FIN
  }
}

note top of P_UI
  Cada «boundary» es una página Next.js.
  Se comunica con su controlador por HTTPS/JSON
  y solo maneja DTO (dependencia a nivel paquete).
end note

note top of P_DOM
  Las entidades tienen getters y setters,
  que no se dibujan para no recargar el diagrama.
end note

note bottom of P_DOM
  Las composiciones sin flecha son navegables en ambos sentidos: CU020, CU021 y CU026
  recorren Pregunta -> Factor -> Competencia (en JPA, @ManyToOne del lado de la parte).
  Copia del modelo (CU025, "mismo modelo"): al registrar la evaluación se copian
  las competencias del puesto, sus factores con 2 o más preguntas activas, esas preguntas
  y sus opciones con ponderación. CU026 sortea 2 preguntas por FactorEvaluado.
  Los parámetros del RepositorioConfiguracion se copian al Cuestionario al crearlo.
end note

' ==========================================================
' NAVEGACIÓN ENTRE PANTALLAS
' ==========================================================
PantallaLogin --> PantallaPrincipal
PantallaLogin --> PantallaCuestionario
PantallaPrincipal --> PantallaImportarCandidatos
PantallaPrincipal --> PantallaGestionarPuestos
PantallaPrincipal --> PantallaGestionarCompetencias
PantallaPrincipal --> PantallaGestionarFactores
PantallaPrincipal --> PantallaGestionarPreguntas
PantallaPrincipal --> PantallaGestionarOpcionesRespuesta
PantallaPrincipal --> PantallaEvaluarCandidatos
PantallaPrincipal --> PantallaReporteComparativo
PantallaPrincipal --> PantallaOrdenMerito
PantallaGestionarPuestos --> PantallaAltaPuesto
PantallaGestionarCompetencias --> PantallaAltaCompetencia
PantallaGestionarFactores --> PantallaAltaFactor
PantallaGestionarPreguntas --> PantallaAltaPregunta
PantallaGestionarPreguntas --> PantallaModificarPregunta
PantallaGestionarPreguntas --> PantallaBajaPregunta
PantallaAltaPregunta --> PantallaAsistenteIA
PantallaGestionarOpcionesRespuesta --> PantallaAltaOpcionRespuesta

' ==========================================================
' PANTALLAS -> CONTROLADORES
' ==========================================================
PantallaLogin ..> AutenticacionController : <<HTTP>>
PantallaImportarCandidatos ..> CandidatoController : <<HTTP>>
PantallaGestionarPuestos ..> PuestoController : <<HTTP>>
PantallaAltaPuesto ..> PuestoController : <<HTTP>>
PantallaAltaPuesto ..> CompetenciaController : <<HTTP>>
PantallaGestionarCompetencias ..> CompetenciaController : <<HTTP>>
PantallaAltaCompetencia ..> CompetenciaController : <<HTTP>>
PantallaGestionarFactores ..> CompetenciaController : <<HTTP>>
PantallaAltaFactor ..> CompetenciaController : <<HTTP>>
PantallaGestionarPreguntas ..> PreguntaController : <<HTTP>>
PantallaGestionarPreguntas ..> CompetenciaController : <<HTTP>>
PantallaAltaPregunta ..> PreguntaController : <<HTTP>>
PantallaAltaPregunta ..> CompetenciaController : <<HTTP>>
PantallaAltaPregunta ..> OpcionRespuestaController : <<HTTP>>
PantallaModificarPregunta ..> PreguntaController : <<HTTP>>
PantallaModificarPregunta ..> OpcionRespuestaController : <<HTTP>>
PantallaBajaPregunta ..> PreguntaController : <<HTTP>>
PantallaAsistenteIA ..> PreguntaController : <<HTTP>>
PantallaAsistenteIA ..> CompetenciaController : <<HTTP>>
PantallaGestionarOpcionesRespuesta ..> OpcionRespuestaController : <<HTTP>>
PantallaAltaOpcionRespuesta ..> OpcionRespuestaController : <<HTTP>>
PantallaEvaluarCandidatos ..> EvaluacionController : <<HTTP>>
PantallaEvaluarCandidatos ..> PuestoController : <<HTTP>>
PantallaCuestionario ..> CuestionarioController : <<HTTP>>
PantallaReporteComparativo ..> ReporteController : <<HTTP>>
PantallaOrdenMerito ..> ReporteController : <<HTTP>>

P_UI ..> P_DTO : usa
P_CTRL ..> P_DTO : usa

' ==========================================================
' CONTROLADORES -> GESTORES
' ==========================================================
AutenticacionController --> GestorAutenticacion
CandidatoController --> GestorImportacionCandidatos
PuestoController --> GestorPuestos
CompetenciaController --> GestorCompetencias
PreguntaController --> GestorPreguntas
OpcionRespuestaController --> GestorOpcionesRespuesta
EvaluacionController --> GestorEvaluaciones
CuestionarioController --> GestorCuestionario
ReporteController --> GestorReportes

' GESTORES -> DTO: cada gestor recibe y devuelve los DTO de sus firmas
GestorAutenticacion ..> CredencialesDTO : transfiere
GestorAutenticacion ..> SesionDTO : transfiere
GestorImportacionCandidatos ..> ResultadoImportacionDTO : transfiere
GestorPuestos ..> FiltroPuestoDTO : transfiere
GestorPuestos ..> PuestoDTO : transfiere
GestorPuestos ..> PuestoAltaDTO : transfiere
GestorPuestos ..> EmpresaDTO : transfiere
GestorPuestos ..> CompetenciaRequeridaDTO : transfiere
GestorCompetencias ..> CompetenciaDTO : transfiere
GestorCompetencias ..> FactorDTO : transfiere
GestorPreguntas ..> FiltroPreguntaDTO : transfiere
GestorPreguntas ..> PreguntaResumenDTO : transfiere
GestorPreguntas ..> PreguntaDTO : transfiere
GestorPreguntas ..> PonderacionDTO : transfiere
GestorPreguntas ..> SolicitudIADTO : transfiere
GestorOpcionesRespuesta ..> OpcionRespuestaDTO : transfiere
GestorOpcionesRespuesta ..> ItemOpcionDTO : transfiere
GestorEvaluaciones ..> FiltroCandidatoDTO : transfiere
GestorEvaluaciones ..> CandidatoGestionDTO : transfiere
GestorEvaluaciones ..> CandidatoClaveDTO : transfiere
GestorEvaluaciones ..> NuevaEvaluacionDTO : transfiere
GestorEvaluaciones ..> EvaluacionDTO : transfiere
GestorEvaluaciones ..> CompetenciaRequeridaDTO : transfiere
GestorEvaluaciones ..> ArchivoDTO : transfiere
GestorCuestionario ..> CuestionarioDTO : transfiere
GestorCuestionario ..> BloqueDTO : transfiere
GestorCuestionario ..> PreguntaCuestionarioDTO : transfiere
GestorCuestionario ..> RespuestaDTO : transfiere
GestorReportes ..> PuestoEstadisticaDTO : transfiere
GestorReportes ..> EvaluacionDTO : transfiere
GestorReportes ..> CandidatoEvaluadoDTO : transfiere
GestorReportes ..> CriterioReporteDTO : transfiere
GestorReportes ..> ReporteDTO : transfiere
ReporteOrdenMerito ..> OrdenMeritoDTO : crea
ReporteOrdenMerito ..> FilaOrdenMeritoDTO : crea
ReporteComparativo ..> ReporteComparativoDTO : crea
ReporteComparativo ..> FilaComparativoDTO : crea
IAsistenteIA ..> PreguntaDTO : devuelve
ExportadorClaves ..> ArchivoDTO : devuelve

NegocioException <|-- ValidacionException
NegocioException <|-- PreguntaDuplicadaException
NegocioException <|-- CompetenciasNoEvaluablesException
NegocioException <|-- RespuestasIncompletasException
NegocioException <|-- ServicioIAException
P_GES ..> P_EXC : lanza
P_CTRL ..> ManejadorErrores
ManejadorErrores ..> ErrorDTO : crea

' composición de DTO (solo datos)
PuestoAltaDTO o-- "1..*" CompetenciaRequeridaDTO
PreguntaDTO o-- "*" PonderacionDTO
OpcionRespuestaDTO o-- "2..*" ItemOpcionDTO
NuevaEvaluacionDTO o-- "1..*" CandidatoClaveDTO
CuestionarioDTO o-- "0..1" BloqueDTO
BloqueDTO o-- "1..*" PreguntaCuestionarioDTO
PreguntaCuestionarioDTO o-- "2..*" ItemOpcionDTO
ReporteDTO <|-- OrdenMeritoDTO
ReporteDTO <|-- ReporteComparativoDTO
OrdenMeritoDTO o-- "*" FilaOrdenMeritoDTO
ReporteComparativoDTO o-- "1..*" FilaComparativoDTO

' ==========================================================
' GESTORES -> DAO, dominio, servicios
' ==========================================================
GestorAutenticacion o--> "2" EstrategiaAutenticacion
GestorAutenticacion --> ServicioToken
EstrategiaAutenticacion <|.. AutenticacionConsultor
EstrategiaAutenticacion <|.. AutenticacionCandidato
AutenticacionConsultor --> IServicioDirectorio
AutenticacionConsultor --> ConsultorDAO
AutenticacionCandidato --> CandidatoDAO
AutenticacionCandidato --> CuestionarioDAO
IServicioDirectorio <|.. AdaptadorLDAP

GestorImportacionCandidatos --> CandidatoDAO
GestorImportacionCandidatos ..> Candidato : procesa

GestorPuestos --> PuestoDAO
GestorPuestos --> EmpresaDAO
GestorPuestos --> CompetenciaDAO
GestorPuestos ..> Puesto : procesa

GestorCompetencias --> CompetenciaDAO
GestorCompetencias --> FactorDAO
GestorCompetencias ..> Competencia : procesa
GestorCompetencias ..> Factor : procesa

GestorPreguntas --> PreguntaDAO
GestorPreguntas --> FactorDAO
GestorPreguntas --> OpcionRespuestaDAO
GestorPreguntas --> ConsultorDAO
GestorPreguntas --> GestorAuditoria
GestorPreguntas --> IAsistenteIA
GestorPreguntas ..> Pregunta : procesa

GestorOpcionesRespuesta --> OpcionRespuestaDAO
GestorOpcionesRespuesta ..> OpcionRespuesta : procesa

GestorAuditoria --> RegistroAuditoriaDAO
GestorAuditoria ..> RegistroAuditoria : procesa

GestorEvaluaciones --> CandidatoDAO
GestorEvaluaciones --> CuestionarioDAO
GestorEvaluaciones --> PuestoDAO
GestorEvaluaciones --> EvaluacionDAO
GestorEvaluaciones --> ConsultorDAO
GestorEvaluaciones --> RepositorioConfiguracion
GestorEvaluaciones --> ExportadorClaves
GestorEvaluaciones ..> Evaluacion : procesa
GestorEvaluaciones ..> Cuestionario : procesa

GestorCuestionario --> CuestionarioDAO
GestorCuestionario --> RegistroEjecucionDAO
GestorCuestionario --> RepositorioConfiguracion
GestorCuestionario ..> Cuestionario : procesa
GestorCuestionario ..> BloqueCuestionario : procesa
GestorCuestionario ..> Resultado : procesa
TareaFinalizarCuestionarios --> GestorCuestionario

GestorReportes o--> "2" EstrategiaReporte
GestorReportes --> PuestoDAO
GestorReportes --> EvaluacionDAO
GestorReportes --> CuestionarioDAO
EstrategiaReporte <|.. ReporteOrdenMerito
EstrategiaReporte <|.. ReporteComparativo
ReporteOrdenMerito --> CuestionarioDAO
ReporteOrdenMerito --> PuestoDAO
ReporteComparativo --> CuestionarioDAO

IAsistenteIA <|.. AdaptadorServicioIA
IAsistenteIA <|.. AsistenteIASimulado
AdaptadorServicioIA --> APIGemini : adapta >
ExportadorClaves <|.. ExportadorExcel
RepositorioConfiguracion <|.. RepositorioConfiguracionProperties

' ==========================================================
' DAO: herencia de interfaces e implementaciones
' ==========================================================
DAO <|-- ConsultorDAO
DAO <|-- CandidatoDAO
DAO <|-- EmpresaDAO
DAO <|-- PuestoDAO
DAO <|-- CompetenciaDAO
DAO <|-- FactorDAO
DAO <|-- PreguntaDAO
DAO <|-- OpcionRespuestaDAO
DAO <|-- EvaluacionDAO
DAO <|-- CuestionarioDAO
DAO <|-- RegistroAuditoriaDAO
DAO <|-- RegistroEjecucionDAO
DAO <|.. DAOJpa

DAOJpa <|-- ConsultorDAOJpa
DAOJpa <|-- CandidatoDAOJpa
DAOJpa <|-- EmpresaDAOJpa
DAOJpa <|-- PuestoDAOJpa
DAOJpa <|-- CompetenciaDAOJpa
DAOJpa <|-- FactorDAOJpa
DAOJpa <|-- PreguntaDAOJpa
DAOJpa <|-- OpcionRespuestaDAOJpa
DAOJpa <|-- EvaluacionDAOJpa
DAOJpa <|-- CuestionarioDAOJpa
DAOJpa <|-- RegistroAuditoriaDAOJpa
DAOJpa <|-- RegistroEjecucionDAOJpa

ConsultorDAO <|.. ConsultorDAOJpa
CandidatoDAO <|.. CandidatoDAOJpa
EmpresaDAO <|.. EmpresaDAOJpa
PuestoDAO <|.. PuestoDAOJpa
CompetenciaDAO <|.. CompetenciaDAOJpa
FactorDAO <|.. FactorDAOJpa
PreguntaDAO <|.. PreguntaDAOJpa
OpcionRespuestaDAO <|.. OpcionRespuestaDAOJpa
EvaluacionDAO <|.. EvaluacionDAOJpa
CuestionarioDAO <|.. CuestionarioDAOJpa
RegistroAuditoriaDAO <|.. RegistroAuditoriaDAOJpa
RegistroEjecucionDAO <|.. RegistroEjecucionDAOJpa

' ==========================================================
' RELACIONES DE DOMINIO
' ==========================================================
Usuario <|-- Consultor
Usuario <|-- Candidato
Usuario ..> Rol

Puesto "*" --> "1" Empresa : pertenece a >
Puesto "1" *-- "1..*" CompetenciaRequerida : requiere >
CompetenciaRequerida "*" --> "1" Competencia : refiere a >

Competencia "1" *-- "0..*" Factor : se compone de >
Factor "1" *-- "0..*" Pregunta : contiene >
Pregunta "*" --> "1" OpcionRespuesta : utiliza >
OpcionRespuesta "1" *-- "2..*" ItemOpcionRespuesta : compone >
Pregunta "1" *-- "2..*" PonderacionPreguntaOpcion : pondera >
PonderacionPreguntaOpcion "*" --> "1" ItemOpcionRespuesta : asigna puntaje a >

Evaluacion "*" --> "1" Puesto : evalúa >
Evaluacion "*" --> "1" Consultor : administrada por >
Evaluacion "1" *-- "1..*" CompetenciaEvaluada : congela >
CompetenciaEvaluada "*" --> "1" Competencia : origen >
CompetenciaEvaluada "1" *-- "1..*" FactorEvaluado
FactorEvaluado "*" --> "1" Factor : origen >
FactorEvaluado "1" *-- "2..*" PreguntaEvaluada
PreguntaEvaluada "*" --> "1" Pregunta : origen >
PreguntaEvaluada "1" *-- "2..*" OpcionEvaluada

Evaluacion "1" *-- "1..*" Cuestionario : genera >
Cuestionario "*" --> "1" Candidato : rendido por >
Cuestionario "*" --> "1" EstadoCuestionario : estado >
EstadoCuestionario <|-- EstadoActivo
EstadoCuestionario <|-- EstadoEnProceso
EstadoCuestionario <|-- EstadoCompleto
EstadoCuestionario <|-- EstadoIncompleto
EstadoCuestionario <|-- EstadoSinContestar
FabricaEstadoCuestionario ..> EstadoCuestionario : crea >
ConversorEstadoCuestionario --> FabricaEstadoCuestionario
Cuestionario ..> FabricaEstadoCuestionario : usa en cambiarEstado >
note right of FabricaEstadoCuestionario
  Factory Method: arma el objeto estado correcto
  (Activo, EnProceso, Completo, Incompleto, SinContestar)
  a partir del nombre guardado en cuestionario.estado
  y en cada transición del patrón State.
end note

Cuestionario "1" *-- "0..*" BloqueCuestionario : se divide en >
BloqueCuestionario "*" --> "1..*" PreguntaEvaluada : asigna {ordered} >
BloqueCuestionario "1" *-- "0..*" RespuestaCandidato : registra >
RespuestaCandidato "*" --> "1" PreguntaEvaluada : responde >
RespuestaCandidato "*" --> "1" OpcionEvaluada : elige >

Cuestionario "1" *-- "0..*" Resultado : obtiene >
Resultado "*" --> "1" CompetenciaEvaluada : de >
Resultado "1" *-- "1..*" ResultadoFactor : detalla >
ResultadoFactor "*" --> "1" FactorEvaluado : de >

RegistroAuditoria "*" --> "1" Consultor : realizada por >
RegistroEjecucion ..> AccionEjecucion
Candidato ..> TipoDocumento
Candidato ..> Genero
GestorReportes ..> TipoReporte


' ===== disposición (solo visual) =====
CredencialesDTO -[hidden]down- CompetenciaRequeridaDTO
CredencialesDTO -[hidden]right- SesionDTO
SesionDTO -[hidden]down- CompetenciaDTO
SesionDTO -[hidden]right- ResultadoImportacionDTO
ResultadoImportacionDTO -[hidden]down- FactorDTO
ResultadoImportacionDTO -[hidden]right- EmpresaDTO
EmpresaDTO -[hidden]down- FiltroPreguntaDTO
EmpresaDTO -[hidden]right- FiltroPuestoDTO
FiltroPuestoDTO -[hidden]down- PreguntaResumenDTO
FiltroPuestoDTO -[hidden]right- PuestoDTO
PuestoDTO -[hidden]down- PreguntaDTO
PuestoDTO -[hidden]right- PuestoAltaDTO
PuestoAltaDTO -[hidden]down- PonderacionDTO
CompetenciaRequeridaDTO -[hidden]down- SolicitudIADTO
CompetenciaRequeridaDTO -[hidden]right- CompetenciaDTO
CompetenciaDTO -[hidden]down- OpcionRespuestaDTO
CompetenciaDTO -[hidden]right- FactorDTO
FactorDTO -[hidden]down- ItemOpcionDTO
FactorDTO -[hidden]right- FiltroPreguntaDTO
FiltroPreguntaDTO -[hidden]down- FiltroCandidatoDTO
FiltroPreguntaDTO -[hidden]right- PreguntaResumenDTO
PreguntaResumenDTO -[hidden]down- CandidatoGestionDTO
PreguntaResumenDTO -[hidden]right- PreguntaDTO
PreguntaDTO -[hidden]down- CandidatoClaveDTO
PreguntaDTO -[hidden]right- PonderacionDTO
PonderacionDTO -[hidden]down- NuevaEvaluacionDTO
SolicitudIADTO -[hidden]down- EvaluacionDTO
SolicitudIADTO -[hidden]right- OpcionRespuestaDTO
OpcionRespuestaDTO -[hidden]down- ArchivoDTO
OpcionRespuestaDTO -[hidden]right- ItemOpcionDTO
ItemOpcionDTO -[hidden]down- CuestionarioDTO
ItemOpcionDTO -[hidden]right- FiltroCandidatoDTO
FiltroCandidatoDTO -[hidden]down- BloqueDTO
FiltroCandidatoDTO -[hidden]right- CandidatoGestionDTO
CandidatoGestionDTO -[hidden]down- PreguntaCuestionarioDTO
CandidatoGestionDTO -[hidden]right- CandidatoClaveDTO
CandidatoClaveDTO -[hidden]down- RespuestaDTO
CandidatoClaveDTO -[hidden]right- NuevaEvaluacionDTO
NuevaEvaluacionDTO -[hidden]down- PuestoEstadisticaDTO
EvaluacionDTO -[hidden]down- CriterioReporteDTO
EvaluacionDTO -[hidden]right- ArchivoDTO
ArchivoDTO -[hidden]down- ReporteDTO
ArchivoDTO -[hidden]right- CuestionarioDTO
CuestionarioDTO -[hidden]down- OrdenMeritoDTO
CuestionarioDTO -[hidden]right- BloqueDTO
BloqueDTO -[hidden]down- FilaOrdenMeritoDTO
BloqueDTO -[hidden]right- PreguntaCuestionarioDTO
PreguntaCuestionarioDTO -[hidden]down- CandidatoEvaluadoDTO
PreguntaCuestionarioDTO -[hidden]right- RespuestaDTO
RespuestaDTO -[hidden]down- ReporteComparativoDTO
RespuestaDTO -[hidden]right- PuestoEstadisticaDTO
PuestoEstadisticaDTO -[hidden]down- FilaComparativoDTO
CriterioReporteDTO -[hidden]right- ReporteDTO
ReporteDTO -[hidden]right- OrdenMeritoDTO
OrdenMeritoDTO -[hidden]right- FilaOrdenMeritoDTO
FilaOrdenMeritoDTO -[hidden]right- CandidatoEvaluadoDTO
CandidatoEvaluadoDTO -[hidden]right- ReporteComparativoDTO
ReporteComparativoDTO -[hidden]right- FilaComparativoDTO
ConsultorDAOJpa -[hidden]down- PreguntaDAOJpa
ConsultorDAOJpa -[hidden]right- CandidatoDAOJpa
CandidatoDAOJpa -[hidden]down- OpcionRespuestaDAOJpa
CandidatoDAOJpa -[hidden]right- EmpresaDAOJpa
EmpresaDAOJpa -[hidden]down- EvaluacionDAOJpa
EmpresaDAOJpa -[hidden]right- PuestoDAOJpa
PuestoDAOJpa -[hidden]down- CuestionarioDAOJpa
PuestoDAOJpa -[hidden]right- CompetenciaDAOJpa
CompetenciaDAOJpa -[hidden]down- RegistroAuditoriaDAOJpa
CompetenciaDAOJpa -[hidden]right- FactorDAOJpa
FactorDAOJpa -[hidden]down- RegistroEjecucionDAOJpa
PreguntaDAOJpa -[hidden]right- OpcionRespuestaDAOJpa
OpcionRespuestaDAOJpa -[hidden]right- EvaluacionDAOJpa
EvaluacionDAOJpa -[hidden]right- CuestionarioDAOJpa
CuestionarioDAOJpa -[hidden]right- RegistroAuditoriaDAOJpa
RegistroAuditoriaDAOJpa -[hidden]right- RegistroEjecucionDAOJpa
ConsultorDAO -[hidden]down- PreguntaDAO
ConsultorDAO -[hidden]right- CandidatoDAO
CandidatoDAO -[hidden]down- OpcionRespuestaDAO
CandidatoDAO -[hidden]right- EmpresaDAO
EmpresaDAO -[hidden]down- EvaluacionDAO
EmpresaDAO -[hidden]right- PuestoDAO
PuestoDAO -[hidden]down- CuestionarioDAO
PuestoDAO -[hidden]right- CompetenciaDAO
CompetenciaDAO -[hidden]down- RegistroAuditoriaDAO
CompetenciaDAO -[hidden]right- FactorDAO
FactorDAO -[hidden]down- RegistroEjecucionDAO
PreguntaDAO -[hidden]right- OpcionRespuestaDAO
OpcionRespuestaDAO -[hidden]right- EvaluacionDAO
EvaluacionDAO -[hidden]right- CuestionarioDAO
CuestionarioDAO -[hidden]right- RegistroAuditoriaDAO
RegistroAuditoriaDAO -[hidden]right- RegistroEjecucionDAO
P_UI -[hidden]down- P_CTRL
P_CTRL -[hidden]down- P_GES
P_GES -[hidden]down- P_DAO
P_DAO -[hidden]down- P_JPA

@enduml
