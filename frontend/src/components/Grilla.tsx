"use client";

import type { KeyboardEvent, ReactNode } from "react";

export interface Columna<T> {
  titulo: string;
  valor: (fila: T) => ReactNode;
  /** clases extra de la celda (ancho, alineacion) */
  clase?: string;
}

interface Props<T> {
  columnas: Columna<T>[];
  filas: T[];
  clave: (fila: T) => string | number;
  /** ninguna: solo muestra; simple: una fila; multiple: varias (con casillas) */
  seleccion?: "ninguna" | "simple" | "multiple";
  seleccionadas?: (string | number)[];
  onSeleccion?: (claves: (string | number)[]) => void;
  /** doble clic o Enter sobre una fila */
  onActivar?: (fila: T) => void;
  vacio?: string;
  titulo?: string;
}

/**
 * Grilla con seleccion simple o multiple. Se maneja tambien con teclado:
 * Tab entra a las filas, flechas para moverse y Espacio para seleccionar.
 * Las grillas anchas hacen scroll horizontal adentro de su tarjeta.
 */
export default function Grilla<T>({
  columnas,
  filas,
  clave,
  seleccion = "ninguna",
  seleccionadas = [],
  onSeleccion,
  onActivar,
  vacio = "No hay datos para mostrar",
  titulo,
}: Props<T>) {
  const elegir = (k: string | number) => {
    if (seleccion === "ninguna" || !onSeleccion) return;
    if (seleccion === "simple") onSeleccion([k]);
    else onSeleccion(seleccionadas.includes(k) ? seleccionadas.filter((x) => x !== k) : [...seleccionadas, k]);
  };

  const teclado = (e: KeyboardEvent<HTMLTableRowElement>, fila: T) => {
    const k = clave(fila);
    if (e.key === " ") {
      e.preventDefault();
      elegir(k);
    } else if (e.key === "Enter" && onActivar) {
      e.preventDefault();
      onActivar(fila);
    } else if (e.key === "ArrowDown" || e.key === "ArrowUp") {
      e.preventDefault();
      const destino = (e.key === "ArrowDown" ? e.currentTarget.nextElementSibling : e.currentTarget.previousElementSibling) as HTMLElement | null;
      destino?.focus();
    }
  };

  const todas = seleccion === "multiple" && filas.length > 0 && filas.every((f) => seleccionadas.includes(clave(f)));

  return (
    <div className="mb-4 overflow-hidden rounded-ch border border-borde bg-tarjeta shadow-tarjeta">
      {titulo && <div className="border-b border-borde px-4 py-2.5 text-[13px] font-semibold">{titulo}</div>}
      <div className="overflow-x-auto">
        <table className="w-full border-collapse text-[13px]">
          <thead>
            <tr>
              {seleccion === "multiple" && (
                <th className="w-10 border-b border-borde bg-encabezado px-3 py-2.5">
                  <input
                    type="checkbox"
                    aria-label="Seleccionar todas"
                    checked={todas}
                    onChange={() => onSeleccion?.(todas ? [] : filas.map(clave))}
                    className="accent-primary"
                  />
                </th>
              )}
              {columnas.map((c) => (
                <th key={c.titulo} className={`whitespace-nowrap border-b border-borde bg-encabezado px-3 py-2.5 text-left text-[11.5px] font-semibold uppercase tracking-wide text-muted ${c.clase ?? ""}`}>
                  {c.titulo}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filas.length === 0 && (
              <tr>
                <td colSpan={columnas.length + (seleccion === "multiple" ? 1 : 0)} className="px-3 py-6 text-center text-muted">
                  {vacio}
                </td>
              </tr>
            )}
            {filas.map((fila) => {
              const k = clave(fila);
              const marcada = seleccionadas.includes(k);
              return (
                <tr
                  key={k}
                  tabIndex={seleccion === "ninguna" ? undefined : 0}
                  aria-selected={seleccion === "ninguna" ? undefined : marcada}
                  onClick={() => elegir(k)}
                  onDoubleClick={() => onActivar?.(fila)}
                  onKeyDown={(e) => teclado(e, fila)}
                  className={`h-[37px] border-b border-borde last:border-b-0 ${seleccion !== "ninguna" ? "cursor-pointer outline-none focus-visible:bg-primary-softer" : ""} ${
                    marcada ? "bg-primary-soft" : seleccion !== "ninguna" ? "hover:bg-encabezado" : ""
                  }`}
                >
                  {seleccion === "multiple" && (
                    <td className="px-3 py-2">
                      <input type="checkbox" tabIndex={-1} aria-label="Seleccionar fila" checked={marcada} onChange={() => elegir(k)} onClick={(e) => e.stopPropagation()} className="accent-primary" />
                    </td>
                  )}
                  {columnas.map((c) => (
                    <td key={c.titulo} className={`px-3 py-2 ${c.clase ?? ""}`}>
                      {c.valor(fila)}
                    </td>
                  ))}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
