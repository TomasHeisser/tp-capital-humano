import type { PreguntaCuestionarioDTO } from "@/lib/tipos";

interface Props {
  numero: number;
  pregunta: PreguntaCuestionarioDTO;
  elegida: number | undefined;
  onElegir: (idOpcion: number) => void;
  faltante: boolean;
}

/** Una pregunta del bloque con sus opciones (una sola se puede marcar). */
export default function PreguntaBloque({ numero, pregunta, elegida, onElegir, faltante }: Props) {
  const nombre = `pregunta-${pregunta.idPregunta}`;
  return (
    <fieldset
      className={`mb-3 rounded-ch border bg-tarjeta px-4 py-3 shadow-tarjeta ${faltante ? "border-danger bg-danger-fondo" : "border-borde"}`}
      aria-invalid={faltante || undefined}
    >
      <legend className="sr-only">Pregunta {numero}</legend>
      <p className="mb-2.5 text-[13.5px] font-semibold">
        {numero}. {pregunta.enunciado}
      </p>
      <div className="flex flex-wrap gap-x-5 gap-y-2">
        {pregunta.opciones.map((o) => (
          <label key={o.id} className="flex cursor-pointer items-center gap-2 text-[13px]">
            <input
              type="radio"
              name={nombre}
              value={o.id ?? ""}
              checked={elegida === o.id}
              onChange={() => o.id !== null && onElegir(o.id)}
              className="h-4 w-4 accent-primary"
            />
            {o.descripcion}
          </label>
        ))}
      </div>
    </fieldset>
  );
}
