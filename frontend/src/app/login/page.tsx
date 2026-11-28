"use client";

import { useRef, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import BarraSuperior from "@/components/BarraSuperior";
import Boton from "@/components/Boton";
import CampoContrasenia from "@/components/CampoContrasenia";
import CampoTexto from "@/components/CampoTexto";
import Combo from "@/components/Combo";
import Dialogo from "@/components/Dialogo";
import { api, ErrorApi } from "@/lib/api";
import { M01, M02 } from "@/lib/mensajes";
import { guardarSesion, inicioDe } from "@/lib/sesion";
import { TIPOS_DOCUMENTO, type CredencialesDTO, type Rol, type SesionDTO, type TipoDocumento } from "@/lib/tipos";

type Paso = "tipo" | "consultor" | "candidato";

/**
 * PantallaLogin (CU001): primero se elige el tipo de acceso y despues se piden
 * usuario y contrasenia (consultor) o documento y clave (candidato).
 */
export default function PantallaLogin() {
  const router = useRouter();
  const [paso, setPaso] = useState<Paso>("tipo");
  const [tipoAcceso, setTipoAcceso] = useState<Rol>("CONSULTOR");
  const [usuario, setUsuario] = useState("");
  const [contrasenia, setContrasenia] = useState("");
  const [tipoDocumento, setTipoDocumento] = useState("");
  const [nroDocumento, setNroDocumento] = useState("");
  const [clave, setClave] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [cerrar, setCerrar] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const primerCampo = useRef<HTMLInputElement & HTMLTextAreaElement>(null);

  const blanquear = () => {
    setUsuario("");
    setContrasenia("");
    setTipoDocumento("");
    setNroDocumento("");
    setClave("");
  };

  // en cualquier paso se puede volver a elegir el tipo de acceso
  const cambiarTipo = () => {
    blanquear();
    setPaso("tipo");
  };

  const salir = () => {
    blanquear();
    setPaso("tipo");
    setCerrar(true);
  };

  const continuar = (e: FormEvent) => {
    e.preventDefault();
    setPaso(tipoAcceso === "CONSULTOR" ? "consultor" : "candidato");
  };

  const ingresar = async (e: FormEvent) => {
    e.preventDefault();
    const cred: CredencialesDTO =
      paso === "consultor"
        ? { tipoAcceso: "CONSULTOR", usuario, contrasenia }
        : { tipoAcceso: "CANDIDATO", tipoDocumento: (tipoDocumento || undefined) as TipoDocumento | undefined, nroDocumento, clave };
    setEnviando(true);
    try {
      const sesion = await api.post<SesionDTO>("/api/auth/login", cred);
      guardarSesion(sesion);
      router.replace(inicioDe(sesion.rol));
    } catch (err) {
      // 5.A y 3.A.2.1: se muestra el error y, al cerrarlo, se blanquean los campos
      const mensaje = err instanceof ErrorApi && err.status !== 400 ? err.message : paso === "consultor" ? M01 : M02;
      setError(mensaje);
    } finally {
      setEnviando(false);
    }
  };

  const cerrarError = () => {
    setError(null);
    blanquear();
    setTimeout(() => primerCampo.current?.focus(), 0);
  };

  return (
    <div className="min-h-screen">
      <BarraSuperior />
      <main className="flex justify-center px-4 pt-16 sm:pt-24">
        <div className="w-full max-w-[620px] rounded-ch border border-borde bg-tarjeta p-6 shadow-tarjeta sm:p-7">
          {paso === "tipo" && (
            <form onSubmit={continuar}>
              <h1 className="text-[20px] font-semibold tracking-tight">Ingreso al sistema</h1>
              <p className="mb-5 text-[13px] text-muted">Seleccione el tipo de acceso</p>
              <fieldset className="mb-6 space-y-3">
                <legend className="sr-only">Tipo de acceso</legend>
                {(
                  [
                    ["CONSULTOR", "Ingresar como usuario consultor"],
                    ["CANDIDATO", "Realizar cuestionario"],
                  ] as const
                ).map(([valor, texto]) => (
                  <label key={valor} className="flex cursor-pointer items-center gap-2.5 text-[14px]">
                    <input
                      type="radio"
                      name="tipoAcceso"
                      value={valor}
                      checked={tipoAcceso === valor}
                      onChange={() => setTipoAcceso(valor)}
                      autoFocus={valor === tipoAcceso}
                      className="h-4 w-4 accent-primary"
                    />
                    {texto}
                  </label>
                ))}
              </fieldset>
              <div className="flex justify-end gap-2.5">
                <Boton type="submit" variante="primario">
                  Continuar
                </Boton>
                <Boton onClick={salir}>Salir</Boton>
              </div>
            </form>
          )}

          {paso === "consultor" && (
            <form onSubmit={ingresar} noValidate>
              <h1 className="text-[20px] font-semibold tracking-tight">Ingreso de consultor</h1>
              <p className="mb-5 text-[13px] text-muted">Tipo de acceso: usuario consultor</p>
              <div className="mb-6 space-y-3.5">
                <CampoTexto ref={primerCampo} id="usuario" etiqueta="Usuario" valor={usuario} onCambio={setUsuario} maxLength={15} mayusculas autoFocus />
                <CampoContrasenia id="contrasenia" etiqueta="Contraseña" valor={contrasenia} onCambio={setContrasenia} maxLength={20} />
              </div>
              <Botones enviando={enviando} onCambiarTipo={cambiarTipo} onSalir={salir} />
            </form>
          )}

          {paso === "candidato" && (
            <form onSubmit={ingresar} noValidate>
              <h1 className="text-[20px] font-semibold tracking-tight">Realizar cuestionario</h1>
              <p className="mb-5 text-[13px] text-muted">Tipo de acceso: candidato</p>
              <div className="mb-6 space-y-3.5">
                <Combo
                  id="tipoDocumento"
                  etiqueta="Tipo de documento"
                  valor={tipoDocumento}
                  onCambio={setTipoDocumento}
                  opciones={TIPOS_DOCUMENTO.map((t) => ({ valor: t, texto: t }))}
                  autoFocus
                />
                <CampoTexto ref={primerCampo} id="nroDocumento" etiqueta="Nro. de documento" valor={nroDocumento} onCambio={setNroDocumento} maxLength={20} mayusculas />
                <CampoContrasenia id="clave" etiqueta="Clave" valor={clave} onCambio={setClave} maxLength={8} mayusculas />
              </div>
              <Botones enviando={enviando} onCambiarTipo={cambiarTipo} onSalir={salir} />
            </form>
          )}
        </div>
      </main>

      <Dialogo abierto={error !== null} titulo="Error" botones={[{ texto: "Aceptar", onClick: cerrarError, primario: true }]}>
        {error}
      </Dialogo>
      <Dialogo abierto={cerrar} titulo="Salir" botones={[{ texto: "Aceptar", onClick: () => setCerrar(false), primario: true }]}>
        No hay ninguna sesión abierta. Puede cerrar esta ventana del navegador.
      </Dialogo>
    </div>
  );
}

function Botones({ enviando, onCambiarTipo, onSalir }: { enviando: boolean; onCambiarTipo: () => void; onSalir: () => void }) {
  return (
    <div className="flex flex-wrap gap-2.5">
      <Boton type="submit" variante="primario" disabled={enviando}>
        Ingresar
      </Boton>
      <Boton onClick={onCambiarTipo}>Cambiar tipo de acceso</Boton>
      <Boton onClick={onSalir} className="sm:ml-auto">
        Salir
      </Boton>
    </div>
  );
}
