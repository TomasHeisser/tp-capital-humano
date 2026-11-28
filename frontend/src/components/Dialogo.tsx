"use client";

import { useEffect, type ReactNode } from "react";
import Boton from "./Boton";

export interface BotonDialogo {
  texto: string;
  onClick: () => void;
  primario?: boolean;
  peligro?: boolean;
  /** si ningun boton lo pide, el foco va al primero */
  foco?: boolean;
}

interface Props {
  abierto: boolean;
  titulo: string;
  children: ReactNode;
  botones: BotonDialogo[];
  /** para los carteles que se cierran "presionando cualquier tecla" */
  cualquierTecla?: () => void;
}

/**
 * Dialogo centrado sobre un fondo oscurecido (confirmaciones, informativos y errores de una sola linea).
 * El foco arranca en el primer boton, asi Enter lo presiona.
 */
export default function Dialogo({ abierto, titulo, children, botones, cualquierTecla }: Props) {
  useEffect(() => {
    if (!abierto || !cualquierTecla) return;
    // se registra en el proximo ciclo para no agarrar la misma tecla que abrio el dialogo
    let escuchar: ((e: KeyboardEvent) => void) | null = null;
    const t = setTimeout(() => {
      escuchar = (e: KeyboardEvent) => {
        e.preventDefault();
        cualquierTecla();
      };
      window.addEventListener("keydown", escuchar, { once: true });
    }, 0);
    return () => {
      clearTimeout(t);
      if (escuchar) window.removeEventListener("keydown", escuchar);
    };
  }, [abierto, cualquierTecla]);

  if (!abierto) return null;
  return (
    <div className="no-imprimir fixed inset-0 z-50 flex items-center justify-center bg-texto/45 px-4">
      <div role="dialog" aria-modal="true" aria-labelledby="titulo-dialogo" className="w-full max-w-[480px] rounded-ch border border-borde bg-tarjeta shadow-dialogo">
        <div id="titulo-dialogo" className="border-b border-borde px-5 py-3 text-[14px] font-semibold">
          {titulo}
        </div>
        <div className="px-5 py-4 text-[13.5px] leading-relaxed">{children}</div>
        <div className="flex justify-end gap-2.5 px-5 pb-4">
          {botones.map((b, i) => (
            <Boton key={b.texto} variante={b.peligro ? "peligro" : b.primario ? "primario" : "secundario"} autoFocus={botones.some((x) => x.foco) ? !!b.foco : i === 0} onClick={b.onClick}>
              {b.texto}
            </Boton>
          ))}
        </div>
      </div>
    </div>
  );
}
