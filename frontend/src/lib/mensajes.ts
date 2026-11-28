// Tabla de mensajes del sistema (M01 a M38), con los mismos textos del documento de entradas y salidas.

export const M01 = "El usuario o la contraseña no son válidos";
export const M02 = "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato";
export const M03 = (creados: number, actualizados: number, eliminados: number, conError: number) =>
  `La importación de candidatos finalizó con éxito. Creados: ${creados}, actualizados: ${actualizados}, eliminados: ${eliminados}, filas con error: ${conError}`;
export const M04 = "No se pudo importar el archivo:";
export const M05 = (entidad: string) => `No se pudo registrar ${entidad}. Corrija lo siguiente:`;
export const M06_CRITERIO = "Debe ingresar al menos un criterio de búsqueda";
export const M06_FILTRO = "Debe completar al menos un filtro";
export const M07_CODIGO = (entidad: string, codigo: string) => `Ya existe ${entidad} con el código ${codigo}`;
export const M07_NOMBRE = (entidad: string, nombre: string) => `Ya existe ${entidad} con el nombre ${nombre}`;
export const M08_REPETIDA = (competencia: string) => `La competencia ${competencia} ya fue agregada`;
export const M08_RANGO = "La ponderación debe ser un valor entre 0 y 10";
export const M09 = "No se pueden agregar ni quitar competencias: ya existen cuestionarios generados para este puesto";
export const M10 = (nombre: string) => `El puesto ${nombre} se ha creado correctamente ¿Desea cargar otro?`;
export const M11 = (nombre: string) => `La competencia ${nombre} se ha creado correctamente ¿Desea cargar otra?`;
export const M12 = (nombre: string) => `El factor ${nombre} se ha creado correctamente ¿Desea cargar otro?`;
export const M13 = (nombre: string) => `La Pregunta ${nombre} se ha creado correctamente ¿Desea cargar otra?`;
export const M14 = (nombre: string) => `La opción ${nombre} se ha creado correctamente ¿Desea cargar otra?`;
export const M15 = "La operación ha culminado con éxito";
export const M16 = (nombre: string) => `Los datos del puesto ${nombre} serán eliminados del sistema`;
export const M17 = (nombre: string) => `Los datos del puesto ${nombre} han sido eliminados del sistema.`;
export const M18 = (nombre: string) => `El puesto ${nombre} está siendo usado en la base de datos y no puede eliminarse`;
export const M19 = (orden: number, competencia: string) => `El nro de orden ${orden} ya existe para la competencia ${competencia}`;
export const M20 = "Ya existe una pregunta con ese nombre y/o enunciado para la competencia y el factor";
export const M21 = (suma: string) => `Las ponderaciones deben ser 0 o positivas y sumar 10 (actualmente suman ${suma})`;
export const M22 = (nombre: string) => `Los datos de la pregunta ${nombre} serán eliminados del sistema`;
export const M23 = (nombre: string) => `Los datos de la pregunta ${nombre} han sido eliminados del sistema.`;
export const M24 = "Ocurrió un error al comunicarse con el servicio de IA. Intente nuevamente más tarde";
export const M25_ORDEN = "El orden de visualización no es consecutivo";
export const M25_REPETIDA = (descripcion: string) => `La descripción ${descripcion} está más de una vez en la opción de respuesta`;
export const M26 = "Se debe seleccionar uno o más candidatos";
export const M27 = "Los siguientes candidatos poseen cuestionarios activos o en proceso y no pueden ser evaluados nuevamente:";
export const M28 = "Las siguientes competencias del puesto no pueden ser evaluadas:";
export const M29 = "Se debe seleccionar una función";
export const M30 = "¿Desea exportar la lista de candidatos con sus claves a un archivo Excel?";
export const M31 = "Se superó la cantidad máxima de accesos permitidos. El cuestionario pasa a estado INCOMPLETO.";
export const M32 = "Se superó el tiempo máximo para completar el cuestionario. El cuestionario pasa a estado INCOMPLETO.";
export const M33 = "El cuestionario superó el tiempo máximo permitido en estado activo y pasa a estado SIN CONTESTAR.";
export const M34 = "No se puede continuar. Faltan responder las siguientes preguntas:";
export const M35 = "El Cuestionario ha sido completado con éxito";
export const M36 = "Se debe seleccionar al menos un candidato";
export const M37 = (nombre: string) => `El puesto ${nombre} no posee evaluaciones`;
export const M38 = "Funcionalidad fuera del alcance de esta versión";

// titulo que va arriba del panel de errores
export const TITULO_PANEL_ERRORES = "Revisá los siguientes datos:";
