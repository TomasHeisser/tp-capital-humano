"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import BarraSuperior from "@/components/BarraSuperior";
import Boton from "@/components/Boton";
import Dialogo from "@/components/Dialogo";
import PanelErrores from "@/components/PanelErrores";
import PanelInfo from "@/components/PanelInfo";
import RutaProtegida from "@/components/RutaProtegida";
import Tarjeta from "@/components/Tarjeta";
import TituloPantalla from "@/components/TituloPantalla";
import BarraBloques from "@/components/cuestionario/BarraBloques";
import PreguntaBloque from "@/components/cuestionario/PreguntaBloque";
import { api, ErrorApi } from "@/lib/api";
import { borrarSesion } from "@/lib/sesion";
import type { BloqueDTO, CuestionarioDTO, RespuestaDTO, SesionDTO } from "@/lib/tipos";

/** PantallaCuestionario (CU026): la pantalla del candidato, sin menu. */
export default function PaginaCuestionario() {
  return <RutaProtegida rol="CANDIDATO">{(sesion) => <PantallaCuestionario sesion={sesion} />}</RutaProtegida>;
}

type Vista = { tipo: "cargando" } | { tipo: "instrucciones"; texto: string } | { tipo: "bloque"; bloque: BloqueDTO } | { tipo: "fin" };

// el tamanio de bloque se recuerda (en este navegador) para numerar de corrido aunque reingrese en el ultimo bloque
const claveTamanio = (id: number) => `capitalhumano.tamBloque.${id}`;

function tamanioGuardado(id: number): number {
  try {
    return Number(localStorage.getItem(claveTamanio(id)) ?? 0);
  } catch {
    return 0;
  }
}

function PantallaCuestionario({ sesion }: { sesion: SesionDTO }) {
  const router = useRouter();
  const id = sesion.idCuestionario as number;
  const [vista, setVista] = useState<Vista>({ tipo: "cargando" });
  const [puesto, setPuesto] = useState("");
  const [respuestas, setRespuestas] = useState<Record<number, number>>({});
  const [errores, setErrores] = useState<{ mensaje: string; lista: string[] } | null>(null);
  const [faltantes, setFaltantes] = useState<number[]>([]);
  const [aviso, setAviso] = useState<string | null>(null);
  const [bienvenida, setBienvenida] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const yaIngreso = useRef(false);

  const terminar = useCallback(() => {
    borrarSesion();
    router.replace("/login");
  }, [router]);

  const mostrarBloque = (b: BloqueDTO) => {
    if (!b.esUltimo) {
      try {
        localStorage.setItem(claveTamanio(id), String(b.preguntas.length));
      } catch {
        // si no se puede guardar, se numera dentro del bloque
      }
    }
    setRespuestas({});
    setErrores(null);
    setFaltantes([]);
    setBienvenida(null);
    setVista({ tipo: "bloque", bloque: b });
    window.scrollTo({ top: 0 });
  };

  // muestra lo que devolvio el gestor: mensaje de cierre, instrucciones o el bloque pendiente
  const mostrar = useCallback(
    (dto: CuestionarioDTO) => {
      if (dto.nombrePuesto) setPuesto(dto.nombrePuesto);
      if (dto.bloqueActual) {
        mostrarBloque(dto.bloqueActual);
        // 1.C: el mensaje que acompania al bloque es solo un aviso de bienvenida
        setBienvenida(dto.mensaje);
      } else if (dto.mensaje) {
        setVista({ tipo: "fin" });
        setAviso(dto.mensaje);
      } else {
        setVista({ tipo: "instrucciones", texto: dto.instrucciones ?? "" });
      }
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [],
  );

  // cada ingreso cuenta como acceso: el ref evita contarlo dos veces si React monta el efecto de nuevo
  useEffect(() => {
    if (yaIngreso.current) return;
    yaIngreso.current = true;
    api
      .post<CuestionarioDTO>(`/api/cuestionarios/${id}/ingreso`)
      .then(mostrar)
      .catch((e) => {
        setVista({ tipo: "fin" });
        setAviso(e instanceof ErrorApi ? e.message : "No se pudo abrir el cuestionario");
      });
  }, [id, mostrar]);

  const aceptarInstrucciones = async () => {
    setEnviando(true);
    try {
      mostrarBloque(await api.post<BloqueDTO>(`/api/cuestionarios/${id}/comienzo`));
    } catch (e) {
      setAviso(e instanceof ErrorApi ? e.message : "No se pudo comenzar el cuestionario");
    } finally {
      setEnviando(false);
    }
  };

  const enviarBloque = async (b: BloqueDTO) => {
    const lista: RespuestaDTO[] = Object.entries(respuestas).map(([idPregunta, idOpcion]) => ({ idPregunta: Number(idPregunta), idOpcion }));
    setEnviando(true);
    try {
      mostrar(await api.post<CuestionarioDTO>(`/api/cuestionarios/${id}/bloques/${b.nroBloque}`, lista));
    } catch (e) {
      if (e instanceof ErrorApi && e.status === 400) {
        // 6.A: se queda en el bloque con lo que ya marco y se resaltan las que faltan
        setErrores({ mensaje: e.message, lista: e.errores });
        setFaltantes(b.preguntas.filter((p) => respuestas[p.idPregunta] === undefined).map((p) => p.idPregunta));
        window.scrollTo({ top: 0, behavior: "smooth" });
      } else {
        setAviso(e instanceof ErrorApi ? e.message : "No se pudieron guardar las respuestas");
      }
    } finally {
      setEnviando(false);
    }
  };

  // todos los bloques menos el ultimo tienen el mismo tamanio; si nunca se vio uno, se usa el actual
  const primerNumero = (b: BloqueDTO) => {
    const tam = b.esUltimo ? tamanioGuardado(id) || b.preguntas.length : b.preguntas.length;
    return (b.nroBloque - 1) * tam;
  };

  return (
    <div className="min-h-screen">
      <BarraSuperior usuario={`Candidato: ${sesion.nombreUsuario}`} onSalir={terminar} />
      <main className="mx-auto w-full max-w-[760px] px-4 py-6">
        {vista.tipo === "cargando" && <p className="text-muted">Cargando el cuestionario...</p>}

        {vista.tipo === "instrucciones" && (
          <>
            <TituloPantalla titulo="Cuestionario – Instrucciones" subtitulo={puesto} />
            <Tarjeta>
              <h2 className="mb-2 text-[13px] font-semibold uppercase tracking-wide text-muted">Instrucciones</h2>
              <p className="whitespace-pre-line text-[13.5px] leading-relaxed">{vista.texto}</p>
            </Tarjeta>
            <div className="flex justify-end gap-2.5">
              <Boton variante="primario" onClick={aceptarInstrucciones} disabled={enviando} autoFocus>
                Aceptar
              </Boton>
              {/* 2.A: cancela y el cuestionario sigue Activo */}
              <Boton onClick={terminar}>Cancelar</Boton>
            </div>
          </>
        )}

        {vista.tipo === "bloque" && (
          <form
            onSubmit={(e) => {
              e.preventDefault();
              enviarBloque(vista.bloque);
            }}
          >
            <TituloPantalla titulo="Cuestionario" subtitulo={puesto} />
            <BarraBloques actual={vista.bloque.nroBloque} total={vista.bloque.totalBloques} />
            {bienvenida && !errores && <PanelInfo>{bienvenida}</PanelInfo>}
            {errores && <PanelErrores mensaje={errores.mensaje} errores={errores.lista} />}
            {vista.bloque.preguntas.map((p, i) => (
              <PreguntaBloque
                key={p.idPregunta}
                numero={primerNumero(vista.bloque) + i + 1}
                pregunta={p}
                elegida={respuestas[p.idPregunta]}
                faltante={faltantes.includes(p.idPregunta) && respuestas[p.idPregunta] === undefined}
                onElegir={(idOpcion) => setRespuestas((r) => ({ ...r, [p.idPregunta]: idOpcion }))}
              />
            ))}
            {/* sin boton Anterior: un bloque respondido no se puede volver a ver */}
            <div className="mt-4 flex justify-end">
              <Boton type="submit" variante="primario" disabled={enviando}>
                {vista.bloque.esUltimo ? "Finalizar" : "Siguiente"}
              </Boton>
            </div>
          </form>
        )}

        {vista.tipo === "fin" && <TituloPantalla titulo="Cuestionario" subtitulo={puesto} />}
      </main>

      {/* mensajes M31 a M35: al aceptar termina el caso de uso */}
      <Dialogo
        abierto={aviso !== null}
        titulo="Información"
        botones={[
          {
            texto: "Aceptar",
            primario: true,
            onClick: () => {
              setAviso(null);
              if (vista.tipo === "fin") terminar();
            },
          },
        ]}
      >
        {aviso}
      </Dialogo>
    </div>
  );
}
