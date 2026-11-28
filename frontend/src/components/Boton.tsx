import type { ButtonHTMLAttributes } from "react";

type Variante = "primario" | "secundario" | "peligro";

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: Variante;
}

const estilos: Record<Variante, string> = {
  primario: "bg-primary border-primary text-white hover:bg-primary-hover hover:border-primary-hover",
  secundario: "bg-tarjeta border-borde text-texto hover:bg-encabezado",
  peligro: "bg-danger border-danger text-white hover:opacity-90",
};

/** Boton de 36px. El primario es la accion principal de la pantalla. */
export default function Boton({ variante = "secundario", className = "", type = "button", ...resto }: Props) {
  return (
    <button
      type={type}
      className={`inline-flex h-9 items-center justify-center gap-2 whitespace-nowrap rounded-ch border px-4 text-[13px] font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-45 ${estilos[variante]} ${className}`}
      {...resto}
    />
  );
}
