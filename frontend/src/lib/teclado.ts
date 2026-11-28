// Ayudas de teclado y de ingreso de datos.
// Shift+Tab y Enter sobre un boton ya los resuelve el navegador; aca va lo que no.

/** Los literales se ingresan siempre en mayusculas, sin importar el bloq mayus. */
export function aMayusculas(texto: string): string {
  return texto.toLocaleUpperCase("es-AR");
}

/** Solo digitos (para numeros enteros en un textbox). */
export function soloDigitos(texto: string): string {
  return texto.replace(/\D/g, "");
}

/** Numero con decimales escrito con coma o con punto -> number (o null si esta vacio o no es numero). */
export function aNumero(texto: string): number | null {
  const limpio = texto.trim().replace(",", ".");
  if (limpio === "") return null;
  const n = Number(limpio);
  return Number.isFinite(n) ? n : null;
}
