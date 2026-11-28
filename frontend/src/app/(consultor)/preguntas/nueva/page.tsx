"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Boton from "@/components/Boton";
import Dialogo from "@/components/Dialogo";
import PanelErrores from "@/components/PanelErrores";
import Tarjeta from "@/components/Tarjeta";
import TituloPantalla from "@/components/TituloPantalla";
import FormularioPregunta, { aDTO, aModelo, MODELO_VACIO, type FormularioPreguntaRef, type ModeloPregunta } from "@/components/preguntas/FormularioPregunta";
import PantallaAsistenteIA from "@/components/preguntas/PantallaAsistenteIA";
import { VOLVER_A_GESTION } from "@/components/preguntas/ultimaBusqueda";
import { api, ErrorApi } from "@/lib/api";
import { M13 } from "@/lib/mensajes";
import type { CompetenciaDTO, OpcionRespuestaDTO, PreguntaDTO } from "@/lib/tipos";

/**
 * PantallaAltaPregunta (CU021): formulario con la grilla de ponderaciones,
 * "Generar con IA" (CU033) y "¿Desea cargar otra?" al terminar.
 */
export default function PantallaAltaPregunta() {
  const router = useRouter();
  const formulario = useRef<FormularioPreguntaRef>(null);
  const [competencias, setCompetencias] = useState<CompetenciaDTO[]>([]);
  const [opciones, setOpciones] = useState<OpcionRespuestaDTO[]>([]);
  const [modelo, setModelo] = useState<ModeloPregunta>(MODELO_VACIO);
  const [mensaje, setMensaje] = useState<string | undefined>();
  const [errores, setErrores] = useState<string[]>([]);
  const [creada, setCreada] = useState<string | null>(null);
  const [iaAbierta, setIaAbierta] = useState(false);
  const [enviando, setEnviando] = useState(false);

  // paso 1: datos para los combos
  useEffect(() => {
    api.get<CompetenciaDTO[]>("/api/competencias").then(setCompetencias).catch(() => setCompetencias([]));
    api.get<OpcionRespuestaDTO[]>("/api/opciones-respuesta").then(setOpciones).catch(() => setOpciones([]));
  }, []);

  const limpiarErrores = () => {
    setMensaje(undefined);
    setErrores([]);
  };

  // paso 2: registrar; los errores vuelven todos juntos y se muestran en el panel
  const aceptar = async (e: FormEvent) => {
    e.preventDefault();
    if (enviando) return;
    limpiarErrores();
    setEnviando(true);
    try {
      await api.post<boolean>("/api/preguntas", aDTO(modelo, opciones, null));
      setCreada(modelo.nombre.trim());
    } catch (err) {
      if (err instanceof ErrorApi) {
        setMensaje(err.errores.length > 0 ? err.message : undefined);
        setErrores(err.todos);
        if (err.status === 409) setTimeout(() => formulario.current?.enfocarNombre(), 0);
      } else {
        setErrores(["No se pudo registrar la pregunta"]);
      }
      window.scrollTo({ top: 0 });
    } finally {
      setEnviando(false);
    }
  };

  // 5.A: SI blanquea el formulario; NO vuelve a la gestion (paso 6)
  const cargarOtra = () => {
    setCreada(null);
    setModelo(MODELO_VACIO);
    limpiarErrores();
    setTimeout(() => formulario.current?.enfocarPrimero(), 0);
  };

  // CU033 paso 4: precarga lo que propuso la IA (las ponderaciones las carga el consultor)
  const precargar = (p: PreguntaDTO) => {
    setIaAbierta(false);
    limpiarErrores();
    setModelo(aModelo(p));
  };

  return (
    <>
      <TituloPantalla titulo="Dar de alta pregunta" subtitulo="Preguntas > Alta" />
      <PanelErrores mensaje={mensaje} errores={errores} />
      <Tarjeta>
        <form onSubmit={aceptar} noValidate>
          <FormularioPregunta ref={formulario} modelo={modelo} onCambio={setModelo} competencias={competencias} opciones={opciones} errores={errores} />
          <div className="mt-[18px] flex flex-wrap items-center gap-2.5">
            <Boton onClick={() => setIaAbierta(true)}>Generar con IA</Boton>
            <Boton type="submit" variante="primario" className="ml-auto" disabled={enviando}>
              Aceptar
            </Boton>
            <Boton onClick={() => router.push(VOLVER_A_GESTION)}>Cancelar</Boton>
          </div>
        </form>
      </Tarjeta>

      {iaAbierta && (
        <PantallaAsistenteIA
          competencias={competencias}
          idCompetencia={modelo.idCompetencia}
          idFactor={modelo.idFactor}
          onGenerada={precargar}
          onCancelar={() => setIaAbierta(false)}
        />
      )}

      <Dialogo
        abierto={creada !== null}
        titulo="Confirmación"
        botones={[
          { texto: "SI", onClick: cargarOtra, primario: true },
          { texto: "NO", onClick: () => router.push(VOLVER_A_GESTION) },
        ]}
      >
        {M13(creada ?? "")}
      </Dialogo>
    </>
  );
}
