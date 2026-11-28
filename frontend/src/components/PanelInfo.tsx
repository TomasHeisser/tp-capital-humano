import type { ReactNode } from "react";

/** Recuadro informativo (por ejemplo el saludo de la pantalla principal). */
export default function PanelInfo({ children, tipo = "info" }: { children: ReactNode; tipo?: "info" | "ok" }) {
  const color = tipo === "ok" ? "border-ok-borde bg-ok-fondo text-ok" : "border-primary-soft bg-primary-softer text-primary";
  return <div className={`mb-4 rounded-ch border px-4 py-3 text-[13px] ${color}`}>{children}</div>;
}
