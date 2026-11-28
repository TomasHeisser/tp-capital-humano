import type { ReactNode } from "react";
import { fechaHora } from "@/lib/formato";

interface Props {
  titulo: string;
  emitido: string;
  usuario: string;
  /** lineas de la izquierda debajo del titulo (por ejemplo el puesto) */
  detalle?: ReactNode;
  /** lineas extra de la derecha (evaluaciones, funcion, candidato) */
  datos: string[];
}

/** Encabezado de los reportes impresos: logo, consultora, titulo, fecha de emision y usuario. */
export default function EncabezadoReporte({ titulo, emitido, usuario, detalle, datos }: Props) {
  return (
    <div className="mb-4 grid gap-3 rounded-ch border border-borde bg-tarjeta p-4 text-[12.5px] sm:grid-cols-[auto_1fr_auto]">
      <div className="flex h-11 w-11 items-center justify-center rounded-ch bg-primary text-[15px] font-bold text-white print:[print-color-adjust:exact]">
        C@
      </div>
      <div>
        <p className="font-semibold">Capit@l Humano – Consultora de RR.HH.</p>
        <p className="font-semibold">{titulo}</p>
        {detalle}
      </div>
      <div className="text-muted sm:text-right">
        <p>Emitido: {fechaHora(emitido)}</p>
        <p>Usuario: {usuario}</p>
        {datos.map((d) => (
          <p key={d}>{d}</p>
        ))}
      </div>
    </div>
  );
}
