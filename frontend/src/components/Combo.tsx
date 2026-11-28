"use client";

import Campo, { claseControl } from "./Campo";

export interface OpcionCombo {
  valor: string;
  texto: string;
}

interface Props {
  id: string;
  etiqueta: string;
  opciones: OpcionCombo[];
  valor: string;
  onCambio: (valor: string) => void;
  /** texto de la opcion vacia ("Seleccione..." por defecto); null para no mostrarla */
  vacio?: string | null;
  error?: boolean;
  disabled?: boolean;
  autoFocus?: boolean;
  className?: string;
}

/** Combo de seleccion simple. El valor vacio ("") es "no eligio nada". */
export default function Combo({ id, etiqueta, opciones, valor, onCambio, vacio = "Seleccione...", error, disabled, autoFocus, className }: Props) {
  return (
    <Campo etiqueta={etiqueta} htmlFor={id} className={className}>
      <select
        id={id}
        name={id}
        value={valor}
        disabled={disabled}
        autoFocus={autoFocus}
        aria-invalid={error || undefined}
        onChange={(e) => onCambio(e.target.value)}
        className={`${claseControl(error)} pr-8`}
      >
        {vacio !== null && <option value="">{vacio}</option>}
        {opciones.map((o) => (
          <option key={o.valor} value={o.valor}>
            {o.texto}
          </option>
        ))}
      </select>
    </Campo>
  );
}
