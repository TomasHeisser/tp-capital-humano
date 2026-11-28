// Cliente de la API REST. Agrega el token, convierte los errores en ErrorApi
// y si el token vencio manda de vuelta al login.
import { borrarSesion, leerSesion } from "./sesion";
import type { ErrorDTO } from "./tipos";

const URL_API = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export class ErrorApi extends Error {
  constructor(
    public readonly status: number,
    mensaje: string,
    public readonly errores: string[] = [],
  ) {
    super(mensaje);
  }

  // mensaje + errores en una lista, para el panel de errores
  get todos(): string[] {
    return this.errores.length > 0 ? this.errores : [this.message];
  }
}

type Parametros = Record<string, string | number | null | undefined>;

function armarUrl(ruta: string, parametros?: Parametros): string {
  const url = new URL(URL_API + ruta);
  Object.entries(parametros ?? {}).forEach(([k, v]) => {
    if (v !== null && v !== undefined && String(v).trim() !== "") url.searchParams.set(k, String(v).trim());
  });
  return url.toString();
}

async function pedir(metodo: string, ruta: string, cuerpo?: unknown, parametros?: Parametros): Promise<Response> {
  const sesion = leerSesion();
  const encabezados: Record<string, string> = {};
  if (sesion) encabezados["Authorization"] = `Bearer ${sesion.token}`;
  if (cuerpo !== undefined && !(cuerpo instanceof FormData)) encabezados["Content-Type"] = "application/json";

  let respuesta: Response;
  try {
    respuesta = await fetch(armarUrl(ruta, parametros), {
      method: metodo,
      headers: encabezados,
      body: cuerpo === undefined ? undefined : cuerpo instanceof FormData ? cuerpo : JSON.stringify(cuerpo),
    });
  } catch {
    throw new ErrorApi(0, "No se pudo conectar con el servidor. Verifique que el sistema esté en funcionamiento.");
  }

  if (respuesta.status === 401 && ruta !== "/api/auth/login") {
    // sesion vencida o invalida: recarga entera a proposito, asi no queda nada de la sesion en memoria
    borrarSesion();
    // eslint-disable-next-line @next/next/no-location-assign-relative-destination
    window.location.href = "/login";
    throw new ErrorApi(401, "La sesión venció. Vuelva a ingresar.");
  }
  if (!respuesta.ok) {
    let error: ErrorDTO = { mensaje: "Ocurrió un error inesperado. Intente nuevamente", errores: [] };
    try {
      error = (await respuesta.json()) as ErrorDTO;
    } catch {
      // vino sin cuerpo: queda el mensaje generico
    }
    throw new ErrorApi(respuesta.status, error.mensaje, error.errores ?? []);
  }
  return respuesta;
}

async function json<T>(r: Response): Promise<T> {
  const texto = await r.text();
  return (texto ? JSON.parse(texto) : null) as T;
}

export const api = {
  get: async <T>(ruta: string, parametros?: Parametros) => json<T>(await pedir("GET", ruta, undefined, parametros)),
  post: async <T>(ruta: string, cuerpo?: unknown) => json<T>(await pedir("POST", ruta, cuerpo ?? {})),
  put: async <T>(ruta: string, cuerpo: unknown) => json<T>(await pedir("PUT", ruta, cuerpo)),
  del: async <T>(ruta: string) => json<T>(await pedir("DELETE", ruta)),

  /** Sube un archivo (multipart) en el campo indicado. */
  subir: async <T>(ruta: string, campo: string, archivo: File) => {
    const datos = new FormData();
    datos.append(campo, archivo);
    return json<T>(await pedir("POST", ruta, datos));
  },

  /** Baja un archivo y abre el dialogo del navegador para guardarlo. */
  descargar: async (ruta: string, nombrePorDefecto: string) => {
    const r = await pedir("GET", ruta);
    const disposicion = r.headers.get("Content-Disposition") ?? "";
    const coincide = /filename\*?=(?:UTF-8'')?"?([^";]+)"?/i.exec(disposicion);
    const nombre = coincide ? decodeURIComponent(coincide[1]) : nombrePorDefecto;
    const blob = await r.blob();
    const enlace = document.createElement("a");
    enlace.href = URL.createObjectURL(blob);
    enlace.download = nombre;
    enlace.click();
    URL.revokeObjectURL(enlace.href);
    return nombre;
  },
};
