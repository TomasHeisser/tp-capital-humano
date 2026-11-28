"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { inicioDe, leerSesion } from "@/lib/sesion";

// la raiz solo redirige: al login o a la pantalla de inicio de quien ya entro
export default function Inicio() {
  const router = useRouter();
  useEffect(() => {
    const s = leerSesion();
    router.replace(s ? inicioDe(s.rol) : "/login");
  }, [router]);
  return null;
}
