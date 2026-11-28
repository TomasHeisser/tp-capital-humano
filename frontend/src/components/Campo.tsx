import type { ReactNode } from "react";

// estilo comun de los controles (36px de alto, borde rojo si tiene error)
export function claseControl(error?: boolean): string {
  return `h-9 w-full rounded-ch border bg-tarjeta px-2.5 text-[13px] text-texto outline-none transition-shadow focus:border-primary focus:shadow-[0_0_0_3px_var(--color-primary-soft)] disabled:bg-encabezado disabled:text-muted ${
    error ? "border-danger" : "border-borde"
  }`;
}

/** Etiqueta (12px, mayusculas, gris) + control. */
export default function Campo({ etiqueta, htmlFor, children, className = "" }: { etiqueta: string; htmlFor?: string; children: ReactNode; className?: string }) {
  return (
    <div className={`min-w-0 ${className}`}>
      <label htmlFor={htmlFor} className="mb-1.5 block text-[12px] font-semibold uppercase tracking-wide text-muted">
        {etiqueta}
      </label>
      {children}
    </div>
  );
}
