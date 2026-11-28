import type { ReactNode } from "react";

/** Etiqueta redondeada (por ejemplo, el conjunto de opciones de respuesta en una grilla). */
export default function Etiqueta({ children }: { children: ReactNode }) {
  return <span className="inline-block rounded-full bg-primary-soft px-2 py-0.5 text-[11px] font-semibold text-primary">{children}</span>;
}
