"use client";

import { aMayusculas } from "@/lib/teclado";
import Campo, { claseControl } from "./Campo";

interface Props {
  id: string;
  etiqueta: string;
  valor: string;
  onCambio: (valor: string) => void;
  maxLength?: number;
  mayusculas?: boolean;
  error?: boolean;
  autoFocus?: boolean;
}

/** Contrasenia o clave: se ve con asteriscos. */
export default function CampoContrasenia({ id, etiqueta, valor, onCambio, maxLength, mayusculas = false, error, autoFocus }: Props) {
  return (
    <Campo etiqueta={etiqueta} htmlFor={id}>
      <input
        id={id}
        name={id}
        type="password"
        value={valor}
        maxLength={maxLength}
        autoFocus={autoFocus}
        autoComplete="off"
        aria-invalid={error || undefined}
        onChange={(e) => onCambio(mayusculas ? aMayusculas(e.target.value) : e.target.value)}
        className={claseControl(error)}
      />
    </Campo>
  );
}
