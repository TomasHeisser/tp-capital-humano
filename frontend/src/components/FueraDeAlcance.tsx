"use client";

import { useRouter } from "next/navigation";
import { M38 } from "@/lib/mensajes";
import Boton from "./Boton";
import PanelInfo from "./PanelInfo";
import TituloPantalla from "./TituloPantalla";

/** Pantalla provisoria para las opciones del menu que todavia no estan en esta version (M38). */
export default function FueraDeAlcance({ titulo }: { titulo: string }) {
  const router = useRouter();
  return (
    <>
      <TituloPantalla titulo={titulo} />
      <PanelInfo>{M38}</PanelInfo>
      <div className="flex justify-end">
        <Boton onClick={() => router.push("/principal")} autoFocus>
          Salir
        </Boton>
      </div>
    </>
  );
}
