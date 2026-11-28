// Formatos de pantalla (locale es-AR): fechas dd/mm/aaaa, hora HH:mm y puntajes con coma.
import type { EstadoCuestionario } from "./tipos";

const SIN_DATO = "—";

function aFecha(iso: string | null | undefined): Date | null {
  if (!iso) return null;
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? null : d;
}

export function fecha(iso: string | null | undefined): string {
  const d = aFecha(iso);
  return d ? d.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit", year: "numeric" }) : SIN_DATO;
}

export function hora(iso: string | null | undefined): string {
  const d = aFecha(iso);
  return d ? d.toLocaleTimeString("es-AR", { hour: "2-digit", minute: "2-digit", hour12: false }) : SIN_DATO;
}

export function fechaHora(iso: string | null | undefined): string {
  const d = aFecha(iso);
  return d ? `${fecha(iso)} ${hora(iso)}` : SIN_DATO;
}

/** 8.25 -> "8,25" */
export function puntaje(valor: number | null | undefined): string {
  if (valor === null || valor === undefined) return SIN_DATO;
  return valor.toLocaleString("es-AR", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

export function numero(valor: number | null | undefined): string {
  return valor === null || valor === undefined ? SIN_DATO : String(valor);
}

const ESTADOS: Record<EstadoCuestionario, string> = {
  ACTIVO: "Activo",
  EN_PROCESO: "En proceso",
  COMPLETO: "Completo",
  INCOMPLETO: "Incompleto",
  SIN_CONTESTAR: "Sin contestar",
};

export function estado(e: string): string {
  return ESTADOS[e as EstadoCuestionario] ?? e;
}
