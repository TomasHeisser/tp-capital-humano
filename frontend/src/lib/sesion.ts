// La sesion (SesionDTO con el token) se guarda en el sessionStorage de la pestania:
// sobrevive a un F5 pero se pierde al cerrar la pestania.
import type { Rol, SesionDTO } from "./tipos";

const CLAVE = "capitalhumano.sesion";

export function guardarSesion(s: SesionDTO): void {
  sessionStorage.setItem(CLAVE, JSON.stringify(s));
}

export function leerSesion(): SesionDTO | null {
  if (typeof window === "undefined") return null;
  try {
    const texto = sessionStorage.getItem(CLAVE);
    return texto ? (JSON.parse(texto) as SesionDTO) : null;
  } catch {
    return null;
  }
}

export function borrarSesion(): void {
  sessionStorage.removeItem(CLAVE);
}

// a donde va cada rol despues del login
export function inicioDe(rol: Rol): string {
  return rol === "CONSULTOR" ? "/principal" : "/cuestionario";
}
