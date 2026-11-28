"use client";

import { useEffect, useState, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { inicioDe, leerSesion } from "@/lib/sesion";
import type { Rol, SesionDTO } from "@/lib/tipos";

/**
 * Deja ver el contenido solo si hay sesion con el rol pedido.
 * Sin sesion va al login; con otro rol va a su propia pantalla de inicio.
 */
export default function RutaProtegida({ rol, children }: { rol: Rol; children: (sesion: SesionDTO) => ReactNode }) {
  const router = useRouter();
  const [sesion, setSesion] = useState<SesionDTO | null>(null);

  useEffect(() => {
    const s = leerSesion();
    if (!s) router.replace("/login");
    else if (s.rol !== rol) router.replace(inicioDe(s.rol));
    // eslint-disable-next-line react-hooks/set-state-in-effect -- la sesion vive en sessionStorage, solo se puede leer ya montado
    else setSesion(s);
  }, [rol, router]);

  if (!sesion) return <div className="p-8 text-muted">Cargando...</div>;
  return <>{children(sesion)}</>;
}
