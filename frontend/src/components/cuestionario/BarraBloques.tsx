/** Barra de progreso del wizard: un tramo por bloque (los respondidos en gris, el actual remarcado). */
export default function BarraBloques({ actual, total }: { actual: number; total: number }) {
  return (
    <ol className="mb-4 grid gap-1.5" style={{ gridTemplateColumns: `repeat(${total}, minmax(0, 1fr))` }} aria-label="Progreso del cuestionario">
      {Array.from({ length: total }, (_, i) => i + 1).map((n) => (
        <li
          key={n}
          aria-current={n === actual ? "step" : undefined}
          className={`truncate rounded-ch border px-2 py-1.5 text-center text-[12px] ${
            n === actual
              ? "border-primary bg-primary-softer font-semibold text-primary"
              : n < actual
                ? "border-borde bg-encabezado text-muted"
                : "border-borde bg-tarjeta text-muted"
          }`}
        >
          Bloque {n} de {total}
        </li>
      ))}
    </ol>
  );
}
