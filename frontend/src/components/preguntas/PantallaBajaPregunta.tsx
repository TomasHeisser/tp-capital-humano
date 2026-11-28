"use client";

import { useCallback, useState } from "react";
import Dialogo from "@/components/Dialogo";
import { api, ErrorApi } from "@/lib/api";
import { M22, M23 } from "@/lib/mensajes";
import type { PreguntaResumenDTO } from "@/lib/tipos";

type Paso = "confirmar" | "eliminada" | "error";

interface Props {
  pregunta: PreguntaResumenDTO;
  /** eliminada = true si se dio de baja (la gestion actualiza la grilla) */
  onCerrar: (eliminada: boolean) => void;
}

/**
 * PantallaBajaPregunta (CU023): confirmacion, baja y cartel que se cierra con cualquier tecla.
 */
export default function PantallaBajaPregunta({ pregunta, onCerrar }: Props) {
  const [paso, setPaso] = useState<Paso>("confirmar");
  const [error, setError] = useState("");
  const [enviando, setEnviando] = useState(false);

  const eliminar = async () => {
    if (enviando) return;
    setEnviando(true);
    try {
      await api.del<boolean>(`/api/preguntas/${pregunta.id}`);
      setPaso("eliminada");
    } catch (e) {
      setError(e instanceof ErrorApi ? e.message : "No se pudo eliminar la pregunta");
      setPaso("error");
    } finally {
      setEnviando(false);
    }
  };

  // paso 4: cualquier tecla cierra el cartel y la gestion repite la busqueda
  const terminar = useCallback(() => onCerrar(true), [onCerrar]);

  return (
    <>
      <Dialogo
        abierto={paso === "confirmar"}
        titulo="Confirmación"
        botones={[
          { texto: "ELIMINAR", onClick: eliminar, peligro: true },
          { texto: "CANCELAR", onClick: () => onCerrar(false) },
        ]}
      >
        {M22(pregunta.nombre)}
      </Dialogo>
      <Dialogo abierto={paso === "eliminada"} titulo="Información" botones={[{ texto: "Aceptar", onClick: terminar, primario: true }]} cualquierTecla={terminar}>
        <p>{M23(pregunta.nombre)}</p>
        <p className="mt-1 text-[12px] text-muted">Presione cualquier tecla para continuar.</p>
      </Dialogo>
      <Dialogo abierto={paso === "error"} titulo="Error" botones={[{ texto: "Aceptar", onClick: terminar, primario: true }]}>
        {error}
      </Dialogo>
    </>
  );
}
