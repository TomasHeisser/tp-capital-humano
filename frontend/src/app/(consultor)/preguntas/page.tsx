"use client";

import { useCallback, useEffect, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Boton from "@/components/Boton";
import CampoTexto from "@/components/CampoTexto";
import Combo from "@/components/Combo";
import Grilla from "@/components/Grilla";
import PanelErrores from "@/components/PanelErrores";
import Tarjeta from "@/components/Tarjeta";
import TituloPantalla from "@/components/TituloPantalla";
import PantallaBajaPregunta from "@/components/preguntas/PantallaBajaPregunta";
import { guardarUltimaBusqueda, leerUltimaBusqueda } from "@/components/preguntas/ultimaBusqueda";
import { useFactores } from "@/components/preguntas/useFactores";
import { api, ErrorApi } from "@/lib/api";
import type { CompetenciaDTO, FiltroPreguntaDTO, PreguntaResumenDTO } from "@/lib/tipos";

/**
 * PantallaGestionarPreguntas (CU020): busqueda con al menos un criterio y acceso
 * al alta (CU021), la modificacion (CU022) y la baja (CU023).
 */
export default function PantallaGestionarPreguntas() {
  const router = useRouter();
  const [competencias, setCompetencias] = useState<CompetenciaDTO[]>([]);
  const [idCompetencia, setIdCompetencia] = useState("");
  const [idFactor, setIdFactor] = useState("");
  const factores = useFactores(idCompetencia);
  const [nombre, setNombre] = useState("");
  const [preguntas, setPreguntas] = useState<PreguntaResumenDTO[]>([]);
  const [seleccion, setSeleccion] = useState<number | null>(null);
  const [errores, setErrores] = useState<string[]>([]);
  const [aEliminar, setAEliminar] = useState<PreguntaResumenDTO | null>(null);
  const [buscando, setBuscando] = useState(false);


  // pasos 3 y 4: buscar y mostrar la grilla
  const buscar = useCallback(async (filtro: FiltroPreguntaDTO) => {
    setErrores([]);
    setBuscando(true);
    try {
      const filas = await api.get<PreguntaResumenDTO[]>("/api/preguntas", {
        idCompetencia: filtro.idCompetencia,
        idFactor: filtro.idFactor,
        nombre: filtro.nombre,
      });
      guardarUltimaBusqueda(filtro);
      setPreguntas(filas);
      setSeleccion(null);
    } catch (e) {
      setErrores(e instanceof ErrorApi ? e.todos : ["No se pudo realizar la búsqueda"]);
    } finally {
      setBuscando(false);
    }
  }, []);

  // repite la ultima busqueda (al volver de un alta, modificacion o baja)
  const actualizarGrilla = useCallback(() => {
    const ultima = leerUltimaBusqueda();
    if (!ultima) return;
    setIdCompetencia(ultima.idCompetencia ? String(ultima.idCompetencia) : "");
    setIdFactor(ultima.idFactor ? String(ultima.idFactor) : "");
    setNombre(ultima.nombre ?? "");
    void buscar(ultima);
  }, [buscar]);

  // paso 2: combos cargados al abrir; si vuelve de otra pantalla repite la busqueda
  useEffect(() => {
    api.get<CompetenciaDTO[]>("/api/competencias").then(setCompetencias).catch(() => setCompetencias([]));
    if (new URLSearchParams(window.location.search).get("repetir") !== "1") return;
    const t = setTimeout(() => {
      actualizarGrilla();
      router.replace("/preguntas");
    }, 0);
    return () => clearTimeout(t);
  }, [actualizarGrilla, router]);

  const enviar = (e: FormEvent) => {
    e.preventDefault();
    void buscar({
      idCompetencia: idCompetencia ? Number(idCompetencia) : null,
      idFactor: idFactor ? Number(idFactor) : null,
      nombre: nombre.trim(),
    });
  };

  const elegida = preguntas.find((p) => p.id === seleccion) ?? null;

  return (
    <>
      <TituloPantalla titulo="Gestionar preguntas" subtitulo="Preguntas" />
      <PanelErrores errores={errores} />
      <Tarjeta>
        <form onSubmit={enviar} noValidate>
          <div className="grid gap-x-5 gap-y-3.5 lg:grid-cols-2">
            <Combo
              id="competencia"
              etiqueta="Competencia"
              valor={idCompetencia}
              onCambio={(v) => {
                setIdCompetencia(v);
                setIdFactor("");
              }}
              opciones={competencias.map((c) => ({ valor: String(c.id), texto: c.nombre }))}
              vacio="(todas)"
              autoFocus
            />
            <Combo
              id="factor"
              etiqueta="Factor"
              valor={idFactor}
              onCambio={setIdFactor}
              opciones={factores.map((f) => ({ valor: String(f.id), texto: f.nombre }))}
              vacio="(todos)"
            />
            <CampoTexto id="nombre" etiqueta="Nombre de pregunta" valor={nombre} onCambio={setNombre} maxLength={100} mayusculas className="lg:col-span-2" />
          </div>
          <div className="mt-[18px] flex flex-wrap gap-2.5">
            <Boton type="submit" variante="primario" disabled={buscando}>
              Buscar
            </Boton>
            <Boton onClick={() => router.push("/preguntas/nueva")}>Nuevo</Boton>
            <Boton disabled={!elegida} onClick={() => elegida && router.push(`/preguntas/${elegida.id}/modificar`)}>
              Modificar
            </Boton>
            <Boton disabled={!elegida} onClick={() => setAEliminar(elegida)}>
              Eliminar
            </Boton>
            <Boton className="ml-auto" onClick={() => router.push("/principal")}>
              Salir
            </Boton>
          </div>
        </form>
      </Tarjeta>

      <Grilla<PreguntaResumenDTO>
        columnas={[
          { titulo: "Competencia", valor: (p) => p.competencia, clase: "w-[22%]" },
          { titulo: "Factor", valor: (p) => p.factor, clase: "w-[22%]" },
          { titulo: "Pregunta", valor: (p) => p.enunciado },
        ]}
        filas={preguntas}
        clave={(p) => p.id}
        seleccion="simple"
        seleccionadas={seleccion === null ? [] : [seleccion]}
        onSeleccion={(k) => setSeleccion((k[0] as number) ?? null)}
        onActivar={(p) => router.push(`/preguntas/${p.id}/modificar`)}
        vacio="No hay preguntas para mostrar"
      />

      {aEliminar && (
        <PantallaBajaPregunta
          pregunta={aEliminar}
          onCerrar={(eliminada) => {
            setAEliminar(null);
            if (eliminada) actualizarGrilla();
          }}
        />
      )}
    </>
  );
}
