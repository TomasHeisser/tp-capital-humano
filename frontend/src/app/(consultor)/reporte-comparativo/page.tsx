"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import Boton from "@/components/Boton";
import CampoTexto from "@/components/CampoTexto";
import Dialogo from "@/components/Dialogo";
import Grilla from "@/components/Grilla";
import PanelErrores from "@/components/PanelErrores";
import Tarjeta from "@/components/Tarjeta";
import TituloPantalla from "@/components/TituloPantalla";
import EncabezadoReporte from "@/components/reportes/EncabezadoReporte";
import GraficoComparativo from "@/components/reportes/GraficoComparativo";
import { api, ErrorApi } from "@/lib/api";
import { fechaHora, puntaje } from "@/lib/formato";
import { M36 } from "@/lib/mensajes";
import { leerSesion } from "@/lib/sesion";
import type { CandidatoEvaluadoDTO, FilaComparativoDTO, PuestoEstadisticaDTO, ReporteComparativoDTO } from "@/lib/tipos";

/**
 * PantallaReporteComparativo (CU027): se elige la funcion y un candidato que la completo,
 * y se compara lo requerido con lo obtenido en cada competencia (grafico + grilla).
 */
export default function PantallaReporteComparativo() {
  const router = useRouter();
  const [nombre, setNombre] = useState("");
  const [empresa, setEmpresa] = useState("");
  const [puestos, setPuestos] = useState<PuestoEstadisticaDTO[]>([]);
  const [puesto, setPuesto] = useState<PuestoEstadisticaDTO | null>(null);
  const [evaluados, setEvaluados] = useState<CandidatoEvaluadoDTO[]>([]);
  const [elegido, setElegido] = useState<number | null>(null);
  const [reporte, setReporte] = useState<ReporteComparativoDTO | null>(null);
  const [errores, setErrores] = useState<string[]>([]);
  const [aviso, setAviso] = useState<string | null>(null);

  const buscar = async (e: FormEvent) => {
    e.preventDefault();
    setErrores([]);
    try {
      setPuestos(await api.get<PuestoEstadisticaDTO[]>("/api/reportes/puestos", { nombre, empresa }));
      setPuesto(null);
      setEvaluados([]);
      setElegido(null);
    } catch (err) {
      setErrores(err instanceof ErrorApi ? err.todos : ["No se pudo realizar la búsqueda"]);
    }
  };

  const elegirPuesto = async (id: number) => {
    setPuesto(puestos.find((p) => p.id === id) ?? null);
    setElegido(null);
    try {
      setEvaluados(await api.get<CandidatoEvaluadoDTO[]>(`/api/reportes/puestos/${id}/evaluados`));
    } catch (err) {
      setErrores(err instanceof ErrorApi ? err.todos : ["No se pudieron obtener los candidatos"]);
    }
  };

  // paso 6 (6.A sin candidato)
  const comparar = async () => {
    if (!puesto || elegido === null) {
      setAviso(M36);
      return;
    }
    try {
      setReporte(await api.post<ReporteComparativoDTO>("/api/reportes/COMPARATIVO", { idPuesto: puesto.id, idsEvaluaciones: [], idCuestionario: elegido }));
    } catch (err) {
      setErrores(err instanceof ErrorApi ? err.todos : ["No se pudo emitir el reporte"]);
    }
  };

  if (reporte) {
    const candidato = evaluados.find((c) => c.idCuestionario === elegido);
    const documento = candidato ? ` (${candidato.tipoDocumento} ${candidato.nroDocumento})` : "";
    return (
      <>
        <div className="no-imprimir">
          <TituloPantalla titulo="Reporte comparativo – Comparación" subtitulo="Reportes > Reporte comparativo" />
          <p className="mb-3 text-[12.5px] text-muted">
            Candidato: {reporte.candidato}
            {documento} – Función: {reporte.nombrePuesto} – {reporte.empresa}
          </p>
        </div>
        <div className="hidden print:block">
          <EncabezadoReporte
            titulo="REPORTE COMPARATIVO"
            emitido={reporte.fechaEmision}
            usuario={leerSesion()?.nombreUsuario ?? ""}
            datos={[`Función: ${reporte.codigoPuesto} – ${reporte.empresa}`, `Candidato: ${reporte.candidato}`]}
          />
        </div>
        <GraficoComparativo filas={reporte.filas} />
        <Grilla<FilaComparativoDTO>
          columnas={[
            { titulo: "Competencia", valor: (f) => f.competencia },
            { titulo: "Requerido", valor: (f) => puntaje(f.requerido), clase: "w-36" },
            { titulo: "Obtenido", valor: (f) => puntaje(f.obtenido), clase: "w-36" },
          ]}
          filas={reporte.filas}
          clave={(f) => f.competencia}
        />
        <div className="no-imprimir flex justify-end gap-2.5">
          <Boton variante="primario" onClick={() => window.print()} autoFocus>
            Imprimir
          </Boton>
          {/* 8.A: vuelve a la lista de candidatos */}
          <Boton onClick={() => setReporte(null)}>Cancelar</Boton>
        </div>
      </>
    );
  }

  return (
    <>
      <TituloPantalla titulo="Reporte comparativo" subtitulo="Reportes > Reporte comparativo" />
      <PanelErrores errores={errores} />
      <Tarjeta>
        <form onSubmit={buscar} noValidate>
          <div className="grid gap-x-5 gap-y-3.5 lg:grid-cols-2">
            <CampoTexto id="nombre" etiqueta="Nombre de función" valor={nombre} onCambio={setNombre} maxLength={100} mayusculas autoFocus />
            <CampoTexto id="empresa" etiqueta="Empresa" valor={empresa} onCambio={setEmpresa} maxLength={100} mayusculas />
          </div>
          <div className="mt-[18px]">
            <Boton type="submit" variante="primario">
              Buscar
            </Boton>
          </div>
        </form>
      </Tarjeta>
      <Grilla<PuestoEstadisticaDTO>
        columnas={[
          { titulo: "Código", valor: (p) => p.codigo, clase: "w-40" },
          { titulo: "Nombre de función", valor: (p) => p.nombre },
          { titulo: "Empresa", valor: (p) => p.empresa },
        ]}
        filas={puestos}
        clave={(p) => p.id}
        seleccion="simple"
        seleccionadas={puesto ? [puesto.id] : []}
        onSeleccion={(k) => void elegirPuesto(k[0] as number)}
        vacio="No hay funciones para mostrar"
      />
      <Grilla<CandidatoEvaluadoDTO>
        titulo="Candidatos evaluados en la función"
        columnas={[
          { titulo: "Apellido", valor: (c) => c.apellido },
          { titulo: "Nombre", valor: (c) => c.nombre },
          { titulo: "Tipo doc.", valor: (c) => c.tipoDocumento, clase: "w-24" },
          { titulo: "Documento", valor: (c) => c.nroDocumento, clase: "w-32" },
          { titulo: "Nro candidato", valor: (c) => c.nroCandidato, clase: "w-32" },
          { titulo: "Fecha de finalización", valor: (c) => fechaHora(c.fechaFin), clase: "w-44" },
        ]}
        filas={evaluados}
        clave={(c) => c.idCuestionario}
        seleccion="simple"
        seleccionadas={elegido === null ? [] : [elegido]}
        onSeleccion={(k) => setElegido((k[0] as number) ?? null)}
        onActivar={(c) => setElegido(c.idCuestionario)}
        vacio={puesto ? "Ningún candidato completó la evaluación de esta función" : "Seleccione una función"}
      />
      <div className="flex justify-end gap-2.5">
        <Boton variante="primario" onClick={comparar}>
          Comparar
        </Boton>
        <Boton onClick={() => router.push("/principal")}>Cancelar</Boton>
      </div>
      <Dialogo abierto={aviso !== null} titulo="Información" botones={[{ texto: "Aceptar", onClick: () => setAviso(null), primario: true }]}>
        {aviso}
      </Dialogo>
    </>
  );
}
