"use client";

import { forwardRef } from "react";
import { aMayusculas, soloDigitos } from "@/lib/teclado";
import Campo, { claseControl } from "./Campo";

interface Props {
  id: string;
  etiqueta: string;
  valor: string;
  onCambio: (valor: string) => void;
  maxLength?: number;
  /** literales: se convierten a mayusculas al escribir */
  mayusculas?: boolean;
  /** solo numeros enteros */
  numerico?: boolean;
  multilinea?: boolean;
  error?: boolean;
  disabled?: boolean;
  autoFocus?: boolean;
  placeholder?: string;
  className?: string;
}

/** Textbox con etiqueta. Si multilinea es true usa un textarea. */
const CampoTexto = forwardRef<HTMLInputElement & HTMLTextAreaElement, Props>(function CampoTexto(
  { id, etiqueta, valor, onCambio, maxLength, mayusculas = false, numerico = false, multilinea = false, error, disabled, autoFocus, placeholder, className },
  ref,
) {
  const cambiar = (texto: string) => {
    let v = mayusculas ? aMayusculas(texto) : texto;
    if (numerico) v = soloDigitos(v);
    onCambio(v);
  };
  const comunes = {
    id,
    name: id,
    value: valor,
    maxLength,
    disabled,
    autoFocus,
    placeholder,
    "aria-invalid": error || undefined,
  };
  return (
    <Campo etiqueta={etiqueta} htmlFor={id} className={className}>
      {multilinea ? (
        <textarea
          ref={ref}
          {...comunes}
          rows={3}
          onChange={(e) => cambiar(e.target.value)}
          className={`${claseControl(error)} h-auto min-h-[72px] resize-y py-2`}
        />
      ) : (
        <input
          ref={ref}
          {...comunes}
          type="text"
          inputMode={numerico ? "numeric" : undefined}
          autoComplete="off"
          onChange={(e) => cambiar(e.target.value)}
          className={claseControl(error)}
        />
      )}
    </Campo>
  );
});

export default CampoTexto;
