"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { FactorDTO } from "@/lib/tipos";

/** Factores activos de la competencia elegida ("" = ninguna -> lista vacia). */
export function useFactores(idCompetencia: string): FactorDTO[] {
  // se guarda de que competencia es la lista, asi al cambiar no se ven los factores de la anterior
  const [cargados, setCargados] = useState<{ de: string; lista: FactorDTO[] }>({ de: "", lista: [] });

  useEffect(() => {
    if (!idCompetencia) return;
    let vigente = true;
    api
      .get<FactorDTO[]>(`/api/competencias/${idCompetencia}/factores`)
      .then((lista) => vigente && setCargados({ de: idCompetencia, lista }))
      .catch(() => vigente && setCargados({ de: idCompetencia, lista: [] }));
    return () => {
      vigente = false;
    };
  }, [idCompetencia]);

  return idCompetencia && cargados.de === idCompetencia ? cargados.lista : [];
}
