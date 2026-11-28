"use client";

import { useEffect, useRef, useState } from "react";
import Boton from "@/components/Boton";
import Combo from "@/components/Combo";
import Dialogo from "@/components/Dialogo";
import PanelErrores from "@/components/PanelErrores";
import { api, ErrorApi } from "@/lib/api";
import { M24 } from "@/lib/mensajes";
import type { CompetenciaDTO, PreguntaDTO, SolicitudIADTO } from "@/lib/tipos";
import { useFactores } from "./useFactores";

interface Props {
  competencias: CompetenciaDTO[];
  /** competencia y factor que ya estaban elegidos en el alta ("" si no) */
  idCompetencia: string;
  idFactor: string;
  onGenerada: (p: PreguntaDTO) => void;
  onCancelar: () => void;
}

/**
 * PantallaAsistenteIA (CU033): ventana emergente donde se eligen competencia y factor
 * y se le pide la pregunta al servicio de IA.
 */
export default function PantallaAsistenteIA({ competencias, idCompetencia: compInicial, idFactor: factorInicial, onGenerada, onCancelar }: Props) {
  const [idCompetencia, setIdCompetencia] = useState(compInicial);
  const [idFactor, setIdFactor] = useState(factorInicial);
  const factores = useFactores(idCompetencia);
  const [errores, setErrores] = useState<string[]>([]);
  const [mensaje, setMensaje] = useState<string | undefined>();
  const [generando, setGenerando] = useState(false);
  const [errorServicio, setErrorServicio] = useState<string | null>(null);
  // si cancela mientras se genera, la respuesta que llega despues se ignora
  const abierta = useRef(true);
  useEffect(() => {
    abierta.current = true;
    return () => {
      abierta.current = false;
    };
  }, []);


  // Escape = Cancelar (si no esta abierto el cartel de error)
  useEffect(() => {
    const tecla = (e: KeyboardEvent) => {
      if (e.key === "Escape" && !errorServicio) onCancelar();
    };
    window.addEventListener("keydown", tecla);
    return () => window.removeEventListener("keydown", tecla);
  }, [onCancelar, errorServicio]);

  const generar = async () => {
    setErrores([]);
    setMensaje(undefined);
    setGenerando(true);
    const s: SolicitudIADTO = { idCompetencia: idCompetencia ? Number(idCompetencia) : null, idFactor: idFactor ? Number(idFactor) : null };
    try {
      const generada = await api.post<PreguntaDTO>("/api/preguntas/ia", s);
      if (abierta.current) onGenerada(generada);
    } catch (e) {
      if (!abierta.current) return;
      if (e instanceof ErrorApi && e.status === 400) {
        // 2.A: todas las omisiones juntas
        setMensaje(e.message);
        setErrores(e.errores);
      } else {
        // 4.A: no se pudo hablar con el servicio
        setErrorServicio(e instanceof ErrorApi && e.status !== 503 && e.status !== 0 ? e.message : M24);
      }
    } finally {
      if (abierta.current) setGenerando(false);
    }
  };

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-texto/45 px-4">
      <div role="dialog" aria-modal="true" aria-labelledby="titulo-ia" className="w-full max-w-[600px] rounded-ch border border-borde bg-tarjeta shadow-dialogo">
        <div id="titulo-ia" className="border-b border-borde px-5 py-3 text-[14px] font-semibold">
          Generar pregunta con IA
        </div>
        <div className="space-y-3.5 px-5 py-4">
          <PanelErrores mensaje={mensaje} errores={errores} />
          <Combo
            id="ia-competencia"
            etiqueta="Competencia"
            valor={idCompetencia}
            onCambio={(v) => {
              setIdCompetencia(v);
              setIdFactor("");
            }}
            opciones={competencias.map((c) => ({ valor: String(c.id), texto: c.nombre }))}
            error={errores.some((e) => e.includes("Competencia"))}
            autoFocus
          />
          <Combo
            id="ia-factor"
            etiqueta="Factor"
            valor={idFactor}
            onCambio={setIdFactor}
            opciones={factores.map((f) => ({ valor: String(f.id), texto: f.nombre }))}
            vacio={idCompetencia ? "Seleccione..." : "Seleccione competencia primero"}
            error={errores.some((e) => e.includes("Factor"))}
          />
          {generando && (
            <p role="status" className="flex items-center gap-2.5 text-[13px] text-muted">
              <span className="inline-block h-5 w-5 animate-spin rounded-full border-2 border-primary-soft border-t-primary" />
              Generando pregunta con el servicio de IA...
            </p>
          )}
        </div>
        <div className="flex justify-end gap-2.5 px-5 pb-4">
          <Boton variante="primario" onClick={generar} disabled={generando}>
            Generar pregunta
          </Boton>
          <Boton onClick={onCancelar}>Cancelar</Boton>
        </div>
      </div>
      <Dialogo abierto={errorServicio !== null} titulo="Error" botones={[{ texto: "Aceptar", onClick: () => setErrorServicio(null), primario: true }]}>
        {errorServicio}
      </Dialogo>
    </div>
  );
}
