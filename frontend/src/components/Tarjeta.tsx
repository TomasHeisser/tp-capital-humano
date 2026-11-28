import type { ReactNode } from "react";

/** Tarjeta blanca con borde y sombra minima (filtros, grillas y formularios). */
export default function Tarjeta({ children, className = "", sinRelleno = false }: { children: ReactNode; className?: string; sinRelleno?: boolean }) {
  return (
    <div className={`mb-4 rounded-ch border border-borde bg-tarjeta shadow-tarjeta ${sinRelleno ? "" : "px-5 py-[18px]"} ${className}`}>
      {children}
    </div>
  );
}
