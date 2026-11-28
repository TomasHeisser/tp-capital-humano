"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import BarraSuperior from "@/components/BarraSuperior";
import MenuLateral from "@/components/MenuLateral";
import RutaProtegida from "@/components/RutaProtegida";
import { borrarSesion } from "@/lib/sesion";

// marco de todas las pantallas del consultor: barra superior + menu lateral + contenido
export default function LayoutConsultor({ children }: LayoutProps<"/">) {
  const router = useRouter();
  const [menuAbierto, setMenuAbierto] = useState(false);

  const salir = () => {
    borrarSesion();
    router.replace("/login");
  };

  return (
    <RutaProtegida rol="CONSULTOR">
      {(sesion) => (
        <div className="min-h-screen">
          <BarraSuperior usuario={`Consultor: ${sesion.nombreUsuario}`} onSalir={salir} onMenu={() => setMenuAbierto(true)} />
          <div className="flex">
            <MenuLateral abierto={menuAbierto} onCerrar={() => setMenuAbierto(false)} />
            <main className="min-w-0 flex-1 px-4 py-6 sm:px-7">
              <div className="mx-auto max-w-[1440px]">{children}</div>
            </main>
          </div>
        </div>
      )}
    </RutaProtegida>
  );
}
