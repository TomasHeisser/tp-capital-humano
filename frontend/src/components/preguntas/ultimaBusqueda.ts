// Ultimo filtro usado en Gestionar preguntas. Al volver de un alta, modificacion o baja
// se repite esa busqueda (actualizarGrilla). Vive en el sessionStorage de la pestania.
import type { FiltroPreguntaDTO } from "@/lib/tipos";

const CLAVE = "capitalhumano.preguntas.ultimaBusqueda";

export function guardarUltimaBusqueda(f: FiltroPreguntaDTO): void {
  sessionStorage.setItem(CLAVE, JSON.stringify(f));
}

export function leerUltimaBusqueda(): FiltroPreguntaDTO | null {
  try {
    const texto = sessionStorage.getItem(CLAVE);
    return texto ? (JSON.parse(texto) as FiltroPreguntaDTO) : null;
  } catch {
    return null;
  }
}

/** Ruta de vuelta a la gestion, pidiendo que repita la ultima busqueda. */
export const VOLVER_A_GESTION = "/preguntas?repetir=1";
