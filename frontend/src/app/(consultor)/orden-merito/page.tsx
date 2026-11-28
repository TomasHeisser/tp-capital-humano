"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Boton from "@/components/Boton";
import CampoTexto from "@/components/CampoTexto";
import Dialogo from "@/components/Dialogo";
import Grilla, { type Columna } from "@/components/Grilla";
import PanelErrores from "@/components/PanelErrores";
import Tarjeta from "@/components/Tarjeta";
import TituloPantalla from "@/components/TituloPantalla";
import EncabezadoReporte from "@/components/reportes/EncabezadoReporte";
import { api, ErrorApi } from "@/lib/api";
import { fecha, fechaHora, numero, puntaje } from "@/lib/formato";
import { M37 } from "@/lib/mensajes";
import { leerSesion } from "@/lib/sesion";
import type { EvaluacionDTO, FilaOrdenMeritoDTO, OrdenMeritoDTO, PuestoEstadisticaDTO } from "@/lib/tipos";

const estado = (e: string) => e.replace("_", " ");

const COMUNES: Columna<FilaOrdenMeritoDTO>[] = [
  { titulo: "Tipo doc.", valor: (f) => f.tipoDocumento, clase: "w-20" },
  { titulo: "Nro doc.", valor: (f) => f.nroDocumento, clase: "w-28" },
  { titulo: "Apellido", valor: (f) => f.apellido },
  { titulo: "Nombre", valor: (f) => f.nombre },
];

const COLUMNAS_COMPLETOS: Columna<FilaOrdenMeritoDTO>[] = [
  ...COMUNES,
  { titulo: "Puntaje", valor: (f) => puntaje(f.puntaje), clase: "w-20" },
  { titulo: "Fecha inicio", valor: (f) => fechaHora(f.fechaInicio), clase: "w-36" },
  { titulo: "Fecha fin / Último ingreso", valor: (f) => fechaHora(f.fechaFin ?? f.ultimoIngreso), clase: "w-44" },
  { titulo: "Accesos", valor: (f) => numero(f.cantidadAccesos), clase: "w-20" },
];

const COLUMNAS_RESTO: Columna<FilaOrdenMeritoDTO>[] = [
  { titulo: "Estado", valor: (f) => estado(f.estado), clase: "w-32" },
  ...COMUNES,
  { titulo: "Fecha inicio", valor: (f) => fechaHora(f.fechaInicio), clase: "w-36" },
  { titulo: "Último ingreso", valor: (f) => fechaHora(f.ultimoIngreso), clase: "w-36" },
  { titulo: "Accesos", valor: (f) => numero(f.cantidadAccesos), clase: "w-20" },
];

const clave = (f: FilaOrdenMeritoDTO) => `${f.tipoDocumento}-${f.nroDocumento}-${f.estado}-${f.fechaInicio ?? ""}`;

/**
 * PantallaOrdenMerito (CU028): busca la funcion, elige evaluaciones (o TODAS) y emite el
 * listado en tres grupos, listo para imprimir con el dialogo del navegador.
 */
export default function PantallaOrdenMerito() {
  const router = useRouter();
  const [codigo, setCodigo] = useState("");
  const [nombre, setNombre] = useState("");
  const [empresa, setEmpresa] = useState("");
  const [puestos, setPuestos] = useState<PuestoEstadisticaDTO[]>([]);
  const [seleccion, setSeleccion] = useState<number | null>(null);
  const [evaluaciones, setEvaluaciones] = useState<EvaluacionDTO[]>([]);
  const [elegidas, setElegidas] = useState<(string | number)[]>([]);
  const [eligiendo, setEligiendo] = useState(false);
  const [errorEleccion, setErrorEleccion] = useState<string[]>([]);
  const [reporte, setReporte] = useState<OrdenMeritoDTO | null>(null);
  const [errores, setErrores] = useState<string[]>([]);
  const [aviso, setAviso] = useState<string | null>(null);

  const puesto = puestos.find((p) => p.id === seleccion) ?? null;
  const salir = () => router.push("/principal");

  // pasos 3 y 4
  const buscar = async (e: FormEvent) => {
    e.preventDefault();
    setErrores([]);
    try {
      setPuestos(await api.get<PuestoEstadisticaDTO[]>("/api/reportes/puestos", { codigo, nombre, empresa }));
      setSeleccion(null);
    } catch (err) {
      setErrores(err instanceof ErrorApi ? err.todos : ["No se pudo realizar la búsqueda"]);
    }
  };

  // paso 5 (6.A el puesto no posee evaluaciones)
  const pedirEvaluaciones = async () => {
    if (!puesto) return;
    try {
      const lista = await api.get<EvaluacionDTO[]>(`/api/reportes/puestos/${puesto.id}/evaluaciones`);
      if (lista.length === 0) {
        setAviso(M37(puesto.nombre));
        return;
      }
      setEvaluaciones(lista);
      setElegidas(lista.map((ev) => ev.id));
      setErrorEleccion([]);
      setEligiendo(true);
    } catch (err) {
      setErrores(err instanceof ErrorApi ? err.todos : ["No se pudieron obtener las evaluaciones"]);
    }
  };

  // pasos 6 y 7
  const emitir = async () => {
    if (!puesto) return;
    if (elegidas.length === 0) {
      setErrorEleccion(["Debe seleccionar al menos una evaluación"]);
      return;
    }
    try {
      const om = await api.post<OrdenMeritoDTO>("/api/reportes/ORDEN_MERITO", { idPuesto: puesto.id, idsEvaluaciones: elegidas, idCuestionario: null });
      setEligiendo(false);
      setReporte(om);
    } catch (err) {
      setErrorEleccion(err instanceof ErrorApi ? err.todos : ["No se pudo emitir el orden de mérito"]);
    }
  };

  if (reporte) {
    const fechas = evaluaciones.filter((ev) => elegidas.includes(ev.id)).map((ev) => fecha(ev.fechaCreacion));
    const todas = elegidas.length === evaluaciones.length;
    const textoEvaluaciones = `Evaluaciones: ${todas ? "TODAS " : ""}(${fechas.join(fechas.length === 2 ? " y " : ", ")})`;
    const grupos = Object.entries(reporte.restoPorEstado);
    return (
      <>
        <div className="no-imprimir">
          <TituloPantalla titulo="Orden de mérito – Listado" subtitulo="Reportes > Orden de mérito" />
        </div>
        <EncabezadoReporte
          titulo="ORDEN DE MÉRITO"
          emitido={reporte.fechaEmision}
          usuario={leerSesion()?.nombreUsuario ?? ""}
          detalle={<p className="text-muted">Puesto: {reporte.codigoPuesto} – {reporte.nombrePuesto} – {reporte.empresa}</p>}
          datos={[textoEvaluaciones]}
        />
        <h2 className="mb-2 text-[13.5px] font-semibold">1. Orden de mérito (completos que alcanzaron el mínimo en todas las competencias)</h2>
        <Grilla<FilaOrdenMeritoDTO> columnas={COLUMNAS_COMPLETOS} filas={reporte.ordenMerito} clave={clave} vacio="Ningún candidato en este grupo" />
        <h2 className="mb-2 text-[13.5px] font-semibold">2. Completos que no alcanzaron el mínimo</h2>
        <Grilla<FilaOrdenMeritoDTO> columnas={COLUMNAS_COMPLETOS} filas={reporte.noAlcanzaronMinimo} clave={clave} vacio="Ningún candidato en este grupo" />
        <h2 className="mb-2 text-[13.5px] font-semibold">3. Resto de los candidatos agrupados por estado</h2>
        <Grilla<FilaOrdenMeritoDTO> columnas={COLUMNAS_RESTO} filas={grupos.flatMap(([, filas]) => filas)} clave={clave} vacio="Ningún candidato en este grupo" />
        <div className="no-imprimir flex justify-end gap-2.5">
          <Boton variante="primario" onClick={() => window.print()} autoFocus>
            Imprimir
          </Boton>
          <Boton onClick={() => setReporte(null)}>Cerrar</Boton>
        </div>
      </>
    );
  }

  return (
    <>
      <TituloPantalla titulo="Orden de mérito" subtitulo="Reportes > Orden de mérito" />
      <PanelErrores errores={errores} />
      <Tarjeta>
        <form onSubmit={buscar} noValidate>
          <div className="grid gap-x-5 gap-y-3.5 lg:grid-cols-2">
            <CampoTexto id="codigo" etiqueta="Código" valor={codigo} onCambio={setCodigo} maxLength={20} mayusculas autoFocus />
            <CampoTexto id="nombre" etiqueta="Nombre del puesto" valor={nombre} onCambio={setNombre} maxLength={100} mayusculas />
            <CampoTexto id="empresa" etiqueta="Empresa" valor={empresa} onCambio={setEmpresa} maxLength={100} mayusculas className="lg:col-span-2" />
          </div>
          <div className="mt-[18px] flex gap-2.5">
            <Boton type="submit" variante="primario">
              Buscar
            </Boton>
            {/* 2.A */}
            <Boton onClick={salir}>Cancelar</Boton>
          </div>
        </form>
      </Tarjeta>
      <Grilla<PuestoEstadisticaDTO>
        columnas={[
          { titulo: "Código", valor: (p) => p.codigo, clase: "w-32" },
          { titulo: "Nombre del puesto", valor: (p) => p.nombre },
          { titulo: "Empresa", valor: (p) => p.empresa },
          { titulo: "Candidatos", valor: (p) => p.cantidadCandidatos, clase: "w-28" },
          { titulo: "Evaluaciones completas", valor: (p) => p.evaluacionesCompletas, clase: "w-48" },
        ]}
        filas={puestos}
        clave={(p) => p.id}
        seleccion="simple"
        seleccionadas={seleccion === null ? [] : [seleccion]}
        onSeleccion={(k) => setSeleccion((k[0] as number) ?? null)}
        onActivar={(p) => setSeleccion(p.id)}
        vacio="No hay puestos para mostrar"
      />
      <div className="flex justify-end">
        <Boton variante="primario" disabled={!puesto} onClick={pedirEvaluaciones}>
          Emitir el orden de mérito
        </Boton>
      </div>

      {eligiendo && puesto && (
        <div className="fixed inset-0 z-40 flex items-center justify-center bg-texto/45 px-4">
          <div role="dialog" aria-modal="true" aria-labelledby="titulo-evaluaciones" className="w-full max-w-[620px] rounded-ch border border-borde bg-tarjeta shadow-dialogo">
            <div id="titulo-evaluaciones" className="border-b border-borde px-5 py-3 text-[14px] font-semibold">
              Seleccionar evaluaciones – {puesto.nombre}
            </div>
            <div className="px-5 py-4">
              <PanelErrores errores={errorEleccion} />
              <label className="mb-3 flex cursor-pointer items-center gap-2 text-[13px] font-semibold">
                <input
                  type="checkbox"
                  className="h-4 w-4 accent-primary"
                  checked={elegidas.length === evaluaciones.length}
                  onChange={(e) => setElegidas(e.target.checked ? evaluaciones.map((ev) => ev.id) : [])}
                  autoFocus
                />
                TODAS
              </label>
              <Grilla<EvaluacionDTO>
                columnas={[
                  { titulo: "Fecha de creación", valor: (ev) => fechaHora(ev.fechaCreacion) },
                  { titulo: "Cantidad de candidatos", valor: (ev) => ev.cantidadCandidatos },
                ]}
                filas={evaluaciones}
                clave={(ev) => ev.id}
                seleccion="multiple"
                seleccionadas={elegidas}
                onSeleccion={setElegidas}
              />
            </div>
            <div className="flex justify-end gap-2.5 px-5 pb-4">
              <Boton variante="primario" onClick={emitir}>
                Emitir
              </Boton>
              <Boton onClick={() => setEligiendo(false)}>Cancelar</Boton>
            </div>
          </div>
        </div>
      )}

      <Dialogo abierto={aviso !== null} titulo="Información" botones={[{ texto: "Aceptar", onClick: () => setAviso(null), primario: true }]}>
        {aviso}
      </Dialogo>
    </>
  );
}
