"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

// opciones del perfil consultor, en el orden del sitemap
export const OPCIONES_MENU = [
  { ruta: "/candidatos", texto: "Candidatos" },
  { ruta: "/puestos", texto: "Puestos" },
  { ruta: "/competencias", texto: "Competencias" },
  { ruta: "/factores", texto: "Factores" },
  { ruta: "/preguntas", texto: "Preguntas" },
  { ruta: "/opciones-respuesta", texto: "Opciones de respuesta" },
  { ruta: "/evaluar-candidatos", texto: "Evaluar candidatos" },
  { ruta: "/reporte-comparativo", texto: "Reporte comparativo" },
  { ruta: "/orden-merito", texto: "Orden de mérito" },
];

/**
 * Menu lateral blanco: 220px en pantallas grandes, 180px en notebooks chicas
 * y en ventanas de menos de 1024px se esconde y se abre por encima con el boton de la barra.
 */
export default function MenuLateral({ abierto, onCerrar }: { abierto: boolean; onCerrar: () => void }) {
  const ruta = usePathname();
  return (
    <>
      {abierto && <div className="no-imprimir fixed inset-0 z-30 bg-texto/30 lg:hidden" onClick={onCerrar} />}
      <nav
        aria-label="Menú principal"
        className={`no-imprimir fixed top-14 bottom-0 left-0 z-40 w-[220px] overflow-y-auto border-r border-borde bg-tarjeta px-2.5 py-4 transition-transform lg:sticky lg:top-14 lg:h-[calc(100vh-56px)] lg:w-[180px] lg:translate-x-0 xl:w-[220px] ${
          abierto ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        {OPCIONES_MENU.map((o) => {
          const activa = ruta === o.ruta || ruta.startsWith(o.ruta + "/");
          return (
            <Link
              key={o.ruta}
              href={o.ruta}
              onClick={onCerrar}
              aria-current={activa ? "page" : undefined}
              className={`mb-0.5 block rounded-ch px-3 py-[9px] text-[13.5px] ${activa ? "bg-primary-softer font-semibold text-primary" : "text-texto-menu hover:bg-encabezado"}`}
            >
              {o.texto}
            </Link>
          );
        })}
      </nav>
    </>
  );
}
