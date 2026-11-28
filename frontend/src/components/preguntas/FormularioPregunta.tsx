"use client";

import { forwardRef, useImperativeHandle, useRef } from "react";
import Combo from "@/components/Combo";
import CampoTexto from "@/components/CampoTexto";
import Grilla from "@/components/Grilla";
import { claseControl } from "@/components/Campo";
import { aNumero } from "@/lib/teclado";
import type { CompetenciaDTO, ItemOpcionDTO, OpcionRespuestaDTO, PreguntaDTO } from "@/lib/tipos";
import { useFactores } from "./useFactores";

/** Lo que se esta cargando, tal cual esta en los controles (los ids como texto, "" = sin elegir). */
export interface ModeloPregunta {
  idCompetencia: string;
  idFactor: string;
  nombre: string;
  enunciado: string;
  descripcion: string;
  idOpcionRespuesta: string;
  /** ponderacion escrita para cada opcion, por id de item */
  ponderaciones: Record<number, string>;
}

export const MODELO_VACIO: ModeloPregunta = {
  idCompetencia: "",
  idFactor: "",
  nombre: "",
  enunciado: "",
  descripcion: "",
  idOpcionRespuesta: "",
  ponderaciones: {},
};

const texto = (n: number | null | undefined) => (n === null || n === undefined ? "" : String(n));

/** PreguntaDTO (de la API o de la IA) -> modelo del formulario. */
export function aModelo(p: PreguntaDTO): ModeloPregunta {
  const ponderaciones: Record<number, string> = {};
  p.ponderaciones.forEach((x) => {
    if (x.ponderacion !== null) ponderaciones[x.idItem] = String(x.ponderacion).replace(".", ",");
  });
  return {
    idCompetencia: texto(p.idCompetencia),
    idFactor: texto(p.idFactor),
    nombre: p.nombre ?? "",
    enunciado: p.enunciado ?? "",
    descripcion: p.descripcion ?? "",
    idOpcionRespuesta: texto(p.idOpcionRespuesta),
    ponderaciones,
  };
}

/** Opciones del conjunto elegido, por orden. */
function itemsDe(opciones: OpcionRespuestaDTO[], idOpcion: string): ItemOpcionDTO[] {
  const o = opciones.find((x) => String(x.id) === idOpcion);
  return o ? [...o.items].sort((a, b) => a.orden - b.orden) : [];
}

/** Modelo -> PreguntaDTO para mandar a la API (las validaciones las hace el gestor). */
export function aDTO(m: ModeloPregunta, opciones: OpcionRespuestaDTO[], id: number | null): PreguntaDTO {
  const numero = (s: string) => (s === "" ? null : Number(s));
  return {
    id,
    idCompetencia: numero(m.idCompetencia),
    idFactor: numero(m.idFactor),
    nombre: m.nombre,
    enunciado: m.enunciado,
    descripcion: m.descripcion || null,
    idOpcionRespuesta: numero(m.idOpcionRespuesta),
    ponderaciones: itemsDe(opciones, m.idOpcionRespuesta).map((i) => ({
      idItem: i.id as number,
      orden: i.orden,
      descripcion: i.descripcion,
      ponderacion: aNumero(m.ponderaciones[i.id as number] ?? ""),
    })),
  };
}

// que campos marcar en rojo segun los textos de error que devuelve el gestor
function camposConError(errores: string[]) {
  const hay = (...partes: string[]) => errores.some((e) => partes.some((p) => e.includes(p)));
  return {
    competencia: hay("la Competencia."),
    factor: hay("el Factor."),
    nombre: hay("Nombre de pregunta", "con el nombre"),
    enunciado: hay("la Pregunta", "La Pregunta", "con ese enunciado"),
    descripcion: hay("Descripción"),
    opcion: hay("Opción de respuesta"),
    ponderaciones: hay("onderaci"),
  };
}

export interface FormularioPreguntaRef {
  enfocarNombre: () => void;
  enfocarPrimero: () => void;
}

interface Props {
  modelo: ModeloPregunta;
  onCambio: (m: ModeloPregunta) => void;
  competencias: CompetenciaDTO[];
  opciones: OpcionRespuestaDTO[];
  errores: string[];
  /** en la modificacion la competencia y el factor son de solo lectura */
  soloLecturaCompetencia?: boolean;
}

/**
 * Campos de una pregunta (CU021 y CU022): competencia, factor, nombre, pregunta, descripcion,
 * opcion de respuesta y la grilla para ponderar cada opcion. Los factores se cargan al elegir la competencia.
 */
const FormularioPregunta = forwardRef<FormularioPreguntaRef, Props>(function FormularioPregunta(
  { modelo, onCambio, competencias, opciones, errores, soloLecturaCompetencia = false },
  ref,
) {
  const factores = useFactores(modelo.idCompetencia);
  const nombre = useRef<HTMLInputElement & HTMLTextAreaElement>(null);
  const primero = useRef<HTMLDivElement>(null);
  const conError = camposConError(errores);

  useImperativeHandle(ref, () => ({
    enfocarNombre: () => nombre.current?.focus(),
    enfocarPrimero: () => primero.current?.querySelector<HTMLElement>("select:not(:disabled), input")?.focus(),
  }));


  const cambiar = (parcial: Partial<ModeloPregunta>) => onCambio({ ...modelo, ...parcial });
  const items = itemsDe(opciones, modelo.idOpcionRespuesta);
  const suma = items.reduce((s, i) => s + (aNumero(modelo.ponderaciones[i.id as number] ?? "") ?? 0), 0);

  return (
    <div ref={primero}>
      <div className="grid gap-x-5 gap-y-3.5 lg:grid-cols-2">
        <Combo
          id="competencia"
          etiqueta="Competencia"
          valor={modelo.idCompetencia}
          onCambio={(v) => cambiar({ idCompetencia: v, idFactor: "" })}
          opciones={competencias.map((c) => ({ valor: String(c.id), texto: c.nombre }))}
          disabled={soloLecturaCompetencia}
          error={conError.competencia}
        />
        <Combo
          id="factor"
          etiqueta="Factor"
          valor={modelo.idFactor}
          onCambio={(v) => cambiar({ idFactor: v })}
          opciones={factores.map((f) => ({ valor: String(f.id), texto: f.nombre }))}
          vacio={modelo.idCompetencia ? "Seleccione..." : "Seleccione competencia primero"}
          disabled={soloLecturaCompetencia}
          error={conError.factor}
        />
        <CampoTexto
          ref={nombre}
          id="nombre"
          etiqueta="Nombre de pregunta"
          valor={modelo.nombre}
          onCambio={(v) => cambiar({ nombre: v })}
          maxLength={100}
          mayusculas
          error={conError.nombre}
          className="lg:col-span-2"
        />
        <CampoTexto
          id="enunciado"
          etiqueta="Pregunta"
          valor={modelo.enunciado}
          onCambio={(v) => cambiar({ enunciado: v })}
          maxLength={500}
          mayusculas
          multilinea
          error={conError.enunciado}
          className="lg:col-span-2"
        />
        <CampoTexto
          id="descripcion"
          etiqueta="Descripción (opcional)"
          valor={modelo.descripcion}
          onCambio={(v) => cambiar({ descripcion: v })}
          maxLength={500}
          mayusculas
          error={conError.descripcion}
          className="lg:col-span-2"
        />
        <Combo
          id="opcionRespuesta"
          etiqueta="Opción de respuesta"
          valor={modelo.idOpcionRespuesta}
          onCambio={(v) => cambiar({ idOpcionRespuesta: v, ponderaciones: {} })}
          opciones={opciones.map((o) => ({ valor: String(o.id), texto: o.nombre }))}
          error={conError.opcion}
          className="lg:col-span-2"
        />
      </div>

      <div className="mt-4">
        <Grilla<ItemOpcionDTO>
          columnas={[
            { titulo: "Orden", valor: (i) => i.orden, clase: "w-24" },
            { titulo: "Descripción", valor: (i) => i.descripcion },
            {
              titulo: "Ponderación",
              clase: "w-40",
              valor: (i) => (
                <input
                  id={`ponderacion-${i.orden}`}
                  aria-label={`Ponderación de ${i.descripcion}`}
                  inputMode="decimal"
                  autoComplete="off"
                  maxLength={6}
                  value={modelo.ponderaciones[i.id as number] ?? ""}
                  aria-invalid={conError.ponderaciones || undefined}
                  onChange={(e) =>
                    cambiar({ ponderaciones: { ...modelo.ponderaciones, [i.id as number]: e.target.value.replace(/[^\d.,-]/g, "") } })
                  }
                  className={`${claseControl(conError.ponderaciones)} h-7!`}
                />
              ),
            },
          ]}
          filas={items}
          clave={(i) => i.id as number}
          vacio="Elija una opción de respuesta para cargar las ponderaciones"
        />
        <p className="-mt-2 text-right text-[12px] text-muted">
          Suma de ponderaciones: {suma.toLocaleString("es-AR", { maximumFractionDigits: 2 })}
        </p>
      </div>
    </div>
  );
});

export default FormularioPregunta;
