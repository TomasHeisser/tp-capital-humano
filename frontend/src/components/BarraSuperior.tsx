"use client";

/** Barra azul de 56px: logo a la izquierda, usuario y Salir a la derecha. */
export default function BarraSuperior({ usuario, onSalir, onMenu }: { usuario?: string; onSalir?: () => void; onMenu?: () => void }) {
  return (
    <header className="no-imprimir sticky top-0 z-30 flex h-14 items-center justify-between bg-primary px-4 text-white sm:px-6">
      <div className="flex items-center gap-2.5">
        {onMenu && (
          <button type="button" onClick={onMenu} aria-label="Abrir menú" className="mr-1 rounded-ch px-2 py-1 text-[18px] hover:bg-white/15 lg:hidden">
            ☰
          </button>
        )}
        <span className="inline-flex h-[26px] w-[26px] items-center justify-center rounded-ch bg-white/20 text-[13px] font-bold">C@</span>
        <span className="text-[17px] font-bold tracking-tight">Capit@l Humano</span>
      </div>
      <div className="flex items-center gap-3.5 text-[13px]">
        <span className="hidden sm:inline">{usuario ?? "Usuario no autenticado"}</span>
        {onSalir && (
          <button type="button" onClick={onSalir} className="rounded-ch border border-white/40 px-3 py-1.5 hover:bg-white/15">
            Salir
          </button>
        )}
      </div>
    </header>
  );
}
