import { TITULO_PANEL_ERRORES } from "@/lib/mensajes";

interface Props {
  errores: string[];
  /** mensaje general (por ejemplo "No se pudo registrar la pregunta. Corrija lo siguiente:") */
  mensaje?: string;
}

/** Panel rojo arriba del formulario con TODOS los errores juntos (no tapa campos ni botones). */
export default function PanelErrores({ errores, mensaje }: Props) {
  if (errores.length === 0 && !mensaje) return null;
  return (
    <div role="alert" className="mb-4 rounded-ch border border-danger-borde bg-danger-fondo px-4 py-3 text-[13px] text-danger-texto">
      <b className="mb-1 block">{mensaje ?? TITULO_PANEL_ERRORES}</b>
      {errores.length > 0 && (
        <ul className="list-inside list-disc space-y-0.5">
          {errores.map((e) => (
            <li key={e}>{e}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
