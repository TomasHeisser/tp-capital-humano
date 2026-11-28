"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import Grilla from "@/components/Grilla";
import PanelInfo from "@/components/PanelInfo";
import TituloPantalla from "@/components/TituloPantalla";
import { leerSesion } from "@/lib/sesion";

// modulos que ve el perfil consultor (como en el wireframe de la pantalla principal)
const MODULOS = [
  { modulo: "Candidatos", opciones: "Importar candidatos (CU006)", ruta: "/candidatos" },
  { modulo: "Puestos", opciones: "Gestionar puestos, alta (CU008, CU009)", ruta: "/puestos" },
  { modulo: "Competencias y Factores", opciones: "Gestionar y dar de alta (CU012, CU013, CU016, CU017)", ruta: "/competencias" },
  { modulo: "Preguntas y Opciones de respuesta", opciones: "Gestionar, alta, modificación, baja, IA (CU020–CU024, CU029, CU033)", ruta: "/preguntas" },
  { modulo: "Evaluar candidatos", opciones: "Crear evaluaciones y claves (CU025)", ruta: "/evaluar-candidatos" },
  { modulo: "Reportes", opciones: "Reporte comparativo y orden de mérito (CU027, CU028)", ruta: "/orden-merito" },
];

/** PantallaPrincipal: bienvenida y modulos disponibles para el consultor. */
export default function PantallaPrincipal() {
  const [usuario, setUsuario] = useState("");
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- el nombre sale del sessionStorage
    setUsuario(leerSesion()?.nombreUsuario ?? "");
  }, []);

  return (
    <>
      <TituloPantalla titulo="Pantalla principal" subtitulo="Inicio" />
      <PanelInfo>Bienvenido, {usuario}. Seleccione una opción del menú.</PanelInfo>
      <Grilla
        filas={MODULOS}
        clave={(m) => m.modulo}
        columnas={[
          { titulo: "Módulo", valor: (m) => <Link href={m.ruta} className="font-medium text-primary hover:underline">{m.modulo}</Link>, clase: "w-[34%]" },
          { titulo: "Opciones disponibles para el perfil Consultor", valor: (m) => m.opciones },
        ]}
      />
    </>
  );
}
