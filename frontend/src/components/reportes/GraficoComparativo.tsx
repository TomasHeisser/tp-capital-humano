"use client";

import { useState } from "react";
import { puntaje } from "@/lib/formato";
import type { FilaComparativoDTO } from "@/lib/tipos";

// colores de las dos series (validados para daltonismo y contraste sobre blanco)
const REQUERIDO = "#d97706";
const OBTENIDO = "#2563eb";
const ALTO = 200;
const MAXIMO = 10;

/**
 * Barras agrupadas por competencia: lo que pide el puesto contra lo que obtuvo el candidato
 * (escala 0 a 10). La tabla de abajo tiene los mismos numeros.
 */
export default function GraficoComparativo({ filas }: { filas: FilaComparativoDTO[] }) {
  const [activa, setActiva] = useState<string | null>(null);
  const ancho = Math.max(filas.length * 140, 280);
  const y = (v: number) => ALTO - (Math.min(v, MAXIMO) / MAXIMO) * ALTO;

  return (
    <figure className="mb-4 rounded-ch border border-borde bg-tarjeta p-4 print:[print-color-adjust:exact]">
      <div className="mb-2 flex gap-4 text-[12px] text-muted" aria-hidden="true">
        <span className="flex items-center gap-1.5">
          <span className="inline-block h-2.5 w-2.5 rounded-sm" style={{ background: REQUERIDO }} /> Requerido
        </span>
        <span className="flex items-center gap-1.5">
          <span className="inline-block h-2.5 w-2.5 rounded-sm" style={{ background: OBTENIDO }} /> Obtenido
        </span>
      </div>
      <div className="overflow-x-auto">
        <svg
          role="img"
          aria-label="Gráfico de barras: ponderación requerida y puntaje obtenido por competencia"
          viewBox={`-28 -16 ${ancho + 36} ${ALTO + 52}`}
          className="h-[268px] min-w-[320px]"
          style={{ width: ancho + 36 }}
        >
          {[0, 5, 10].map((v) => (
            <g key={v}>
              <line x1={0} x2={ancho} y1={y(v)} y2={y(v)} stroke="#e2e8f0" strokeWidth={1} />
              <text x={-8} y={y(v) + 4} textAnchor="end" fontSize={11} fill="#64748b">
                {v}
              </text>
            </g>
          ))}
          {filas.map((f, i) => {
            const x = i * 140 + 30;
            const marcada = activa === f.competencia;
            return (
              <g key={f.competencia} onMouseEnter={() => setActiva(f.competencia)} onMouseLeave={() => setActiva(null)}>
                {/* zona de hover mas grande que las barras */}
                <rect x={x - 12} y={-16} width={104} height={ALTO + 16} fill="transparent" />
                {[
                  { v: f.requerido, color: REQUERIDO, dx: 0, nombre: "Requerido" },
                  { v: f.obtenido, color: OBTENIDO, dx: 42, nombre: "Obtenido" },
                ].map((b) => (
                  <g key={b.nombre}>
                    <path
                      d={barra(x + b.dx, y(b.v), 38, ALTO - y(b.v))}
                      fill={b.color}
                      opacity={activa && !marcada ? 0.45 : 1}
                    >
                      <title>{`${f.competencia} – ${b.nombre}: ${puntaje(b.v)}`}</title>
                    </path>
                    <text x={x + b.dx + 19} y={y(b.v) - 5} textAnchor="middle" fontSize={11} fill="#0f172a">
                      {puntaje(b.v)}
                    </text>
                  </g>
                ))}
                <text x={x + 40} y={ALTO + 18} textAnchor="middle" fontSize={11} fill="#334155">
                  {f.competencia}
                </text>
              </g>
            );
          })}
          <line x1={0} x2={ancho} y1={ALTO} y2={ALTO} stroke="#94a3b8" strokeWidth={1} />
        </svg>
      </div>
    </figure>
  );
}

// barra con las puntas de arriba redondeadas (4px) y apoyada en la base
function barra(x: number, y: number, w: number, h: number): string {
  if (h <= 0) return "";
  const r = Math.min(4, h, w / 2);
  return `M${x},${y + h} V${y + r} Q${x},${y} ${x + r},${y} H${x + w - r} Q${x + w},${y} ${x + w},${y + r} V${y + h} Z`;
}
