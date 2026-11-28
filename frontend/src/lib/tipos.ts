// DTO de la API (mismos nombres que en el diagrama de clases). Las fechas llegan como texto ISO.

export type Rol = "CONSULTOR" | "CANDIDATO";
export type TipoDocumento = "DNI" | "LE" | "LC" | "PP";
export type TipoReporte = "ORDEN_MERITO" | "COMPARATIVO";
export type EstadoCuestionario = "ACTIVO" | "EN_PROCESO" | "COMPLETO" | "INCOMPLETO" | "SIN_CONTESTAR";

export const TIPOS_DOCUMENTO: TipoDocumento[] = ["DNI", "LE", "LC", "PP"];

export interface ErrorDTO {
  mensaje: string;
  errores: string[];
}

// ---------- autenticacion ----------
export interface CredencialesDTO {
  tipoAcceso: Rol;
  usuario?: string;
  contrasenia?: string;
  tipoDocumento?: TipoDocumento;
  nroDocumento?: string;
  clave?: string;
}

export interface SesionDTO {
  token: string;
  rol: Rol;
  nombreUsuario: string;
  idCuestionario: number | null;
}

// ---------- candidatos ----------
export interface ResultadoImportacionDTO {
  creados: number;
  actualizados: number;
  eliminados: number;
  filasConError: string[];
}

// ---------- puestos ----------
export interface EmpresaDTO {
  id: number;
  nombre: string;
}

export interface FiltroPuestoDTO {
  codigo?: string;
  nombre?: string;
  empresa?: string;
}

export interface PuestoDTO {
  id: number;
  codigo: string;
  nombre: string;
  empresa: string;
}

export interface CompetenciaRequeridaDTO {
  idCompetencia: number;
  nombreCompetencia: string;
  ponderacion: number;
}

export interface PuestoAltaDTO {
  codigo: string;
  nombre: string;
  descripcion: string;
  idEmpresa: number | null;
  competencias: CompetenciaRequeridaDTO[];
}

// ---------- competencias y factores ----------
export interface CompetenciaDTO {
  id: number | null;
  codigo: string;
  nombre: string;
  descripcion: string | null;
}

export interface FactorDTO {
  id: number | null;
  codigo: string;
  nombre: string;
  descripcion: string | null;
  nroOrden: number | null;
  idCompetencia: number | null;
  nombreCompetencia: string | null;
}

// ---------- preguntas ----------
export interface FiltroPreguntaDTO {
  idCompetencia?: number | null;
  idFactor?: number | null;
  nombre?: string;
}

export interface PreguntaResumenDTO {
  id: number;
  competencia: string;
  factor: string;
  nombre: string;
  enunciado: string;
}

export interface PonderacionDTO {
  idItem: number;
  orden: number;
  descripcion: string;
  ponderacion: number | null;
}

export interface PreguntaDTO {
  id: number | null;
  idCompetencia: number | null;
  idFactor: number | null;
  nombre: string;
  enunciado: string;
  descripcion: string | null;
  idOpcionRespuesta: number | null;
  ponderaciones: PonderacionDTO[];
}

export interface SolicitudIADTO {
  idCompetencia: number | null;
  idFactor: number | null;
}

// ---------- opciones de respuesta ----------
export interface ItemOpcionDTO {
  id: number | null;
  orden: number;
  descripcion: string;
}

export interface OpcionRespuestaDTO {
  id: number | null;
  nombre: string;
  descripcion: string | null;
  items: ItemOpcionDTO[];
}

// ---------- evaluaciones ----------
export interface FiltroCandidatoDTO {
  apellido?: string;
  nombre?: string;
  nroCandidato?: number | null;
}

export interface CandidatoGestionDTO {
  id: number;
  nroCandidato: number;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
}

export interface CandidatoClaveDTO {
  idCandidato: number;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  clave: string;
}

export interface NuevaEvaluacionDTO {
  idPuesto: number;
  candidatos: CandidatoClaveDTO[];
}

export interface EvaluacionDTO {
  id: number;
  fechaCreacion: string;
  nombrePuesto: string;
  empresa: string;
  cantidadCandidatos: number;
}

// ---------- cuestionario ----------
export interface PreguntaCuestionarioDTO {
  idPregunta: number;
  enunciado: string;
  opciones: ItemOpcionDTO[];
}

export interface BloqueDTO {
  nroBloque: number;
  totalBloques: number;
  esUltimo: boolean;
  preguntas: PreguntaCuestionarioDTO[];
}

export interface CuestionarioDTO {
  id: number;
  estado: EstadoCuestionario;
  mensaje: string | null;
  instrucciones: string | null;
  nombrePuesto: string | null;
  bloqueActual: BloqueDTO | null;
}

export interface RespuestaDTO {
  idPregunta: number;
  idOpcion: number;
}

// ---------- reportes ----------
export interface PuestoEstadisticaDTO {
  id: number;
  codigo: string;
  nombre: string;
  empresa: string;
  cantidadCandidatos: number;
  evaluacionesCompletas: number;
}

export interface CriterioReporteDTO {
  idPuesto: number;
  idsEvaluaciones: number[];
  idCuestionario: number | null;
}

export interface CandidatoEvaluadoDTO {
  idCuestionario: number;
  apellido: string;
  nombre: string;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  nroCandidato: number;
  fechaFin: string;
}

export interface ReporteDTO {
  codigoPuesto: string;
  nombrePuesto: string;
  empresa: string;
  fechaEmision: string;
}

export interface FilaOrdenMeritoDTO {
  posicion: number | null;
  tipoDocumento: TipoDocumento;
  nroDocumento: string;
  apellido: string;
  nombre: string;
  estado: EstadoCuestionario;
  puntaje: number | null;
  fechaInicio: string | null;
  fechaFin: string | null;
  ultimoIngreso: string | null;
  cantidadAccesos: number | null;
}

export interface OrdenMeritoDTO extends ReporteDTO {
  ordenMerito: FilaOrdenMeritoDTO[];
  noAlcanzaronMinimo: FilaOrdenMeritoDTO[];
  restoPorEstado: Record<string, FilaOrdenMeritoDTO[]>;
}

export interface FilaComparativoDTO {
  competencia: string;
  requerido: number;
  obtenido: number;
}

export interface ReporteComparativoDTO extends ReporteDTO {
  candidato: string;
  filas: FilaComparativoDTO[];
}
