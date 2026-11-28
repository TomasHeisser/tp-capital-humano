"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { useParams, useRouter } from "next/navigation";
import Boton from "@/components/Boton";
import Dialogo from "@/components/Dialogo";
import PanelErrores from "@/components/PanelErrores";
import Tarjeta from "@/components/Tarjeta";
import TituloPantalla from "@/components/TituloPantalla";
import FormularioPregunta, { aDTO, aModelo, MODELO_VACIO, type FormularioPreguntaRef, type ModeloPregunta } from "@/components/preguntas/FormularioPregunta";
import { VOLVER_A_GESTION } from "@/components/preguntas/ultimaBusqueda";
import { api, ErrorApi } from "@/lib/api";
import { M15 } from "@/lib/mensajes";
import type { CompetenciaDTO, OpcionRespuestaDTO, PreguntaDTO } from "@/lib/tipos";

/**
 * PantallaModificarPregunta (CU022): los datos actuales cargados, con competencia y factor
 * de solo lectura. Al terminar avisa "La operación ha culminado con éxito".
 */
export default function PantallaModificarPregunta() {
  const router = useRouter();
  const { id } = useParams<{ id: string }>();
  const formulario = useRef<FormularioPreguntaRef>(null);
  const [competencias, setCompetencias] = useState<CompetenciaDTO[]>([]);
  const [opciones, setOpciones] = useState<OpcionRespuestaDTO[]>([]);
  const [modelo, setModelo] = useState<ModeloPregunta>(MODELO_VACIO);
  const [mensaje, setMensaje] = useState<string | undefined>();
  const [errores, setErrores] = useState<string[]>([]);
  const [cargada, setCargada] = useState(false);
  const [terminada, setTerminada] = useState(false);
  const [enviando, setEnviando] = useState(false);

  // paso 1: datos de la pregunta y de los combos
  useEffect(() => {
    api.get<CompetenciaDTO[]>("/api/competencias").then(setCompetencias).catch(() => setCompetencias([]));
    api.get<OpcionRespuestaDTO[]>("/api/opciones-respuesta").then(setOpciones).catch(() => setOpciones([]));
    api
      .get<PreguntaDTO>(`/api/preguntas/${id}`)
      .then((p) => {
        setModelo(aModelo(p));
        setCargada(true);
        setTimeout(() => formulario.current?.enfocarNombre(), 0);
      })
      .catch((e) => setErrores(e instanceof ErrorApi ? e.todos : ["No se pudo obtener la pregunta"]));
  }, [id]);

  const aceptar = async (e: FormEvent) => {
    e.preventDefault();
    if (enviando || !cargada) return;
    setMensaje(undefined);
    setErrores([]);
    setEnviando(true);
    try {
      await api.put<boolean>(`/api/preguntas/${id}`, aDTO(modelo, opciones, Number(id)));
      setTerminada(true);
    } catch (err) {
      if (err instanceof ErrorApi) {
        setMensaje(err.errores.length > 0 ? err.message : undefined);
        setErrores(err.todos);
        // 2.C.2: el foco vuelve al nombre
        if (err.status === 409) setTimeout(() => formulario.current?.enfocarNombre(), 0);
      } else {
        setErrores(["No se pudo modificar la pregunta"]);
      }
      window.scrollTo({ top: 0 });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <>
      <TituloPantalla titulo="Modificar pregunta" subtitulo="Preguntas > Modificar" />
      <PanelErrores mensaje={mensaje} errores={errores} />
      <Tarjeta>
        <form onSubmit={aceptar} noValidate>
          <FormularioPregunta
            ref={formulario}
            modelo={modelo}
            onCambio={setModelo}
            competencias={competencias}
            opciones={opciones}
            errores={errores}
            soloLecturaCompetencia
          />
          <div className="mt-[18px] flex justify-end gap-2.5">
            <Boton type="submit" variante="primario" disabled={enviando || !cargada}>
              Aceptar
            </Boton>
            <Boton onClick={() => router.push(VOLVER_A_GESTION)}>Cancelar</Boton>
          </div>
        </form>
      </Tarjeta>

      <Dialogo abierto={terminada} titulo="Información" botones={[{ texto: "Aceptar", onClick: () => router.push(VOLVER_A_GESTION), primario: true }]}>
        {M15}
      </Dialogo>
    </>
  );
}
