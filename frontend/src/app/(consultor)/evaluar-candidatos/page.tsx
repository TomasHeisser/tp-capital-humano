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
import { api, ErrorApi } from "@/lib/api";
import { puntaje } from "@/lib/formato";
import { M26, M27, M28, M29, M30 } from "@/lib/mensajes";
import type { CandidatoClaveDTO, CandidatoGestionDTO, CompetenciaRequeridaDTO, EvaluacionDTO, PuestoDTO } from "@/lib/tipos";

type Paso = 1 | 2 | 3;
type Aviso = { titulo: string; texto: string; lista?: string[]; alCerrar?: () => void };

const COLUMNAS_CANDIDATO = [
  { titulo: "Nro", valor: (c: CandidatoGestionDTO) => c.nroCandidato, clase: "w-20" },
  { titulo: "Apellido", valor: (c: CandidatoGestionDTO) => c.apellido },
  { titulo: "Nombre", valor: (c: CandidatoGestionDTO) => c.nombre },
  { titulo: "Tipo doc.", valor: (c: CandidatoGestionDTO) => c.tipoDocumento, clase: "w-24" },
  { titulo: "Nro doc.", valor: (c: CandidatoGestionDTO) => c.nroDocumento, clase: "w-32" },
];

const nombreCompleto = (c: { apellido: string; nombre: string }) => `${c.apellido}, ${c.nombre}`;

/**
 * PantallaEvaluarCandidatos (CU025): 1) buscar y elegir candidatos, 2) elegir la funcion,
 * 3) ver las claves y finalizar; despues ofrece exportar las claves a Excel.
 */
export default function PantallaEvaluarCandidatos() {
  const router = useRouter();
  const [paso, setPaso] = useState<Paso>(1);
  const [aviso, setAviso] = useState<Aviso | null>(null);
  const [errores, setErrores] = useState<string[]>([]);
  const [ocupado, setOcupado] = useState(false);

  // paso 1
  const [apellido, setApellido] = useState("");
  const [nombre, setNombre] = useState("");
  const [nroCandidato, setNroCandidato] = useState("");
  const [encontrados, setEncontrados] = useState<CandidatoGestionDTO[]>([]);
  const [marcadosBusqueda, setMarcadosBusqueda] = useState<(string | number)[]>([]);
  const [aEvaluar, setAEvaluar] = useState<CandidatoGestionDTO[]>([]);
  const [marcadosEvaluar, setMarcadosEvaluar] = useState<(string | number)[]>([]);

  // paso 2
  const [nombrePuesto, setNombrePuesto] = useState("");
  const [empresa, setEmpresa] = useState("");
  const [puestos, setPuestos] = useState<PuestoDTO[]>([]);
  const [puesto, setPuesto] = useState<PuestoDTO | null>(null);
  const [competencias, setCompetencias] = useState<CompetenciaRequeridaDTO[]>([]);

  // paso 3
  const [claves, setClaves] = useState<CandidatoClaveDTO[]>([]);
  const [evaluacion, setEvaluacion] = useState<EvaluacionDTO | null>(null);

  const salir = () => router.push("/principal");
  const fallo = (e: unknown, porDefecto: string) => setErrores(e instanceof ErrorApi ? e.todos : [porDefecto]);

  // ---------------------------------------------------------------- paso 3: buscar candidatos
  const buscarCandidatos = async (e: FormEvent) => {
    e.preventDefault();
    setErrores([]);
    try {
      const lista = await api.get<CandidatoGestionDTO[]>("/api/evaluaciones/candidatos", {
        apellido,
        nombre,
        nroCandidato: nroCandidato || null,
      });
      setEncontrados(lista);
      setMarcadosBusqueda([]);
    } catch (err) {
      fallo(err, "No se pudo realizar la búsqueda");
    }
  };

  // pasos 4 y 5 (4.A sin seleccion)
  const agregar = () => {
    if (marcadosBusqueda.length === 0) {
      setAviso({ titulo: "Información", texto: M26 });
      return;
    }
    const nuevos = encontrados.filter((c) => marcadosBusqueda.includes(c.id) && !aEvaluar.some((a) => a.id === c.id));
    setAEvaluar([...aEvaluar, ...nuevos]);
    setMarcadosBusqueda([]);
  };

  // 5.A
  const quitar = () => {
    if (marcadosEvaluar.length === 0) {
      setAviso({ titulo: "Información", texto: M26 });
      return;
    }
    setAEvaluar(aEvaluar.filter((c) => !marcadosEvaluar.includes(c.id)));
    setMarcadosEvaluar([]);
  };

  // paso 6: nadie puede tener un cuestionario activo o en proceso (6.A)
  const siguienteCandidatos = async () => {
    if (aEvaluar.length === 0) {
      setAviso({ titulo: "Información", texto: M26 });
      return;
    }
    setOcupado(true);
    try {
      const conActivos = await api.post<CandidatoGestionDTO[]>("/api/evaluaciones/verificacion-activos", aEvaluar.map((c) => c.id));
      if (conActivos.length > 0) {
        setAviso({
          titulo: "Información",
          texto: M27,
          lista: conActivos.map((c) => `${c.nroCandidato} ${nombreCompleto(c)} (${c.tipoDocumento} ${c.nroDocumento})`),
        });
        return;
      }
      setErrores([]);
      setPaso(2);
    } catch (err) {
      fallo(err, "No se pudieron verificar los candidatos");
    } finally {
      setOcupado(false);
    }
  };

  // ---------------------------------------------------------------- paso 8: buscar y elegir la funcion
  const buscarPuestos = async (e: FormEvent) => {
    e.preventDefault();
    setErrores([]);
    try {
      setPuestos(await api.get<PuestoDTO[]>("/api/puestos", { nombre: nombrePuesto, empresa }));
      setPuesto(null);
      setCompetencias([]);
    } catch (err) {
      fallo(err, "No se pudo realizar la búsqueda");
    }
  };

  // pasos 8 y 9 (9.A competencias no evaluables: termina el CU)
  const elegirPuesto = async (id: number) => {
    const elegido = puestos.find((p) => p.id === id) ?? null;
    setPuesto(elegido);
    setCompetencias([]);
    if (!elegido) return;
    try {
      setCompetencias(await api.get<CompetenciaRequeridaDTO[]>(`/api/evaluaciones/puestos/${id}/validacion`));
    } catch (err) {
      if (err instanceof ErrorApi && err.status === 422) {
        setAviso({ titulo: "Información", texto: M28, lista: err.errores, alCerrar: salir });
      } else {
        fallo(err, "No se pudo validar la función");
      }
    }
  };

  // paso 10 (10.A sin funcion)
  const siguientePuesto = async () => {
    if (!puesto || competencias.length === 0) {
      setAviso({ titulo: "Información", texto: M29 });
      return;
    }
    setOcupado(true);
    try {
      setClaves(await api.post<CandidatoClaveDTO[]>("/api/evaluaciones/claves", aEvaluar.map((c) => c.id)));
      setErrores([]);
      setPaso(3);
    } catch (err) {
      fallo(err, "No se pudieron generar las claves");
    } finally {
      setOcupado(false);
    }
  };

  // ---------------------------------------------------------------- paso 12: finalizar
  const finalizar = async () => {
    if (!puesto || ocupado) return;
    setOcupado(true);
    try {
      setEvaluacion(await api.post<EvaluacionDTO>("/api/evaluaciones", { idPuesto: puesto.id, candidatos: claves }));
    } catch (err) {
      fallo(err, "No se pudo registrar la evaluación");
      setOcupado(false);
    }
  };

  // pasos 14 a 18: se descarga el Excel (el navegador pregunta donde guardarlo) y termina
  const exportar = async () => {
    if (!evaluacion) return;
    try {
      await api.descargar(`/api/evaluaciones/${evaluacion.id}/claves`, "claves.xlsx");
      salir();
    } catch (err) {
      setEvaluacion(null);
      fallo(err, "No se pudo exportar el archivo");
    }
  };

  const subtitulo = `Evaluar candidatos > Paso ${paso} de 3`;

  return (
    <>
      {paso === 1 && (
        <>
          <TituloPantalla titulo="Evaluar candidatos – Búsqueda de candidatos" subtitulo={subtitulo} />
          <PanelErrores errores={errores} />
          <Tarjeta>
            <form onSubmit={buscarCandidatos} noValidate>
              <div className="grid gap-x-5 gap-y-3.5 lg:grid-cols-2">
                <CampoTexto id="apellido" etiqueta="Apellido" valor={apellido} onCambio={setApellido} maxLength={100} mayusculas autoFocus />
                <CampoTexto id="nombre" etiqueta="Nombre" valor={nombre} onCambio={setNombre} maxLength={100} mayusculas />
                <CampoTexto id="nroCandidato" etiqueta="Número de candidato" valor={nroCandidato} onCambio={setNroCandidato} maxLength={9} numerico />
              </div>
              <div className="mt-[18px]">
                <Boton type="submit" variante="primario">
                  Buscar
                </Boton>
              </div>
            </form>
          </Tarjeta>
          <Grilla<CandidatoGestionDTO>
            columnas={COLUMNAS_CANDIDATO}
            filas={encontrados}
            clave={(c) => c.id}
            seleccion="multiple"
            seleccionadas={marcadosBusqueda}
            onSeleccion={setMarcadosBusqueda}
            vacio="No hay candidatos para mostrar"
          />
          <div className="mb-4 flex gap-2.5">
            <Boton variante="primario" onClick={agregar}>
              Agregar
            </Boton>
            <Boton onClick={quitar}>Quitar</Boton>
          </div>
          <Grilla<CandidatoGestionDTO>
            titulo="Candidatos a evaluar"
            columnas={COLUMNAS_CANDIDATO}
            filas={aEvaluar}
            clave={(c) => c.id}
            seleccion="multiple"
            seleccionadas={marcadosEvaluar}
            onSeleccion={setMarcadosEvaluar}
            vacio="Todavía no agregó candidatos"
          />
          <div className="flex justify-end gap-2.5">
            <Boton variante="primario" onClick={siguienteCandidatos} disabled={ocupado}>
              Siguiente
            </Boton>
            <Boton onClick={salir}>Cancelar</Boton>
          </div>
        </>
      )}

      {paso === 2 && (
        <>
          <TituloPantalla titulo="Evaluar candidatos – Evaluación de candidatos" subtitulo={subtitulo} />
          <p className="mb-3 text-[12.5px] text-muted">Candidatos seleccionados: {aEvaluar.map(nombreCompleto).join(" – ")}</p>
          <PanelErrores errores={errores} />
          <Tarjeta>
            <form onSubmit={buscarPuestos} noValidate>
              <div className="grid gap-x-5 gap-y-3.5 lg:grid-cols-2">
                <CampoTexto id="nombrePuesto" etiqueta="Nombre del puesto" valor={nombrePuesto} onCambio={setNombrePuesto} maxLength={100} mayusculas autoFocus />
                <CampoTexto id="empresa" etiqueta="Empresa" valor={empresa} onCambio={setEmpresa} maxLength={100} mayusculas />
              </div>
              <div className="mt-[18px]">
                <Boton type="submit" variante="primario">
                  Buscar
                </Boton>
              </div>
            </form>
          </Tarjeta>
          <Grilla<PuestoDTO>
            columnas={[
              { titulo: "Código", valor: (p) => p.codigo, clase: "w-40" },
              { titulo: "Nombre del puesto", valor: (p) => p.nombre },
              { titulo: "Empresa", valor: (p) => p.empresa },
            ]}
            filas={puestos}
            clave={(p) => p.id}
            seleccion="simple"
            seleccionadas={puesto ? [puesto.id] : []}
            onSeleccion={(k) => void elegirPuesto(k[0] as number)}
            vacio="No hay puestos para mostrar"
          />
          <Grilla<CompetenciaRequeridaDTO>
            titulo="Competencias del puesto (informativo)"
            columnas={[
              { titulo: "Competencia", valor: (c) => c.nombreCompetencia },
              { titulo: "Ponderación requerida", valor: (c) => puntaje(c.ponderacion), clase: "w-48" },
            ]}
            filas={competencias}
            clave={(c) => c.idCompetencia}
            vacio="Seleccione una función"
          />
          <div className="flex justify-end gap-2.5">
            <Boton variante="primario" onClick={siguientePuesto} disabled={ocupado}>
              Siguiente
            </Boton>
            <Boton onClick={salir}>Cancelar</Boton>
          </div>
        </>
      )}

      {paso === 3 && (
        <>
          <TituloPantalla titulo="Evaluar candidatos – Candidatos a evaluar" subtitulo={subtitulo} />
          <p className="mb-3 text-[12.5px] text-muted">
            Puesto: {puesto?.nombre} – {puesto?.empresa}
          </p>
          <PanelErrores errores={errores} />
          <Grilla<CandidatoClaveDTO>
            columnas={[
              { titulo: "Apellido", valor: (c) => c.apellido },
              { titulo: "Nombre", valor: (c) => c.nombre },
              { titulo: "Tipo documento", valor: (c) => c.tipoDocumento, clase: "w-36" },
              { titulo: "Nro documento", valor: (c) => c.nroDocumento, clase: "w-36" },
              { titulo: "Clave de ingreso", valor: (c) => <span className="font-mono font-semibold">{c.clave}</span>, clase: "w-40" },
            ]}
            filas={claves}
            clave={(c) => c.idCandidato}
          />
          <div className="flex justify-end gap-2.5">
            <Boton variante="primario" onClick={finalizar} disabled={ocupado}>
              Finalizar
            </Boton>
            <Boton onClick={salir} disabled={ocupado && !errores.length}>
              Cancelar
            </Boton>
          </div>
        </>
      )}

      <Dialogo
        abierto={aviso !== null}
        titulo={aviso?.titulo ?? ""}
        botones={[
          {
            texto: "Aceptar",
            primario: true,
            onClick: () => {
              const alCerrar = aviso?.alCerrar;
              setAviso(null);
              alCerrar?.();
            },
          },
        ]}
      >
        <p>{aviso?.texto}</p>
        {aviso?.lista && (
          <ul className="mt-1.5 list-inside list-disc">
            {aviso.lista.map((l) => (
              <li key={l}>{l}</li>
            ))}
          </ul>
        )}
      </Dialogo>

      <Dialogo
        abierto={evaluacion !== null}
        titulo="Confirmación"
        botones={[
          { texto: "SI", onClick: exportar, primario: true },
          { texto: "NO", onClick: salir },
        ]}
      >
        {M30}
      </Dialogo>
    </>
  );
}
