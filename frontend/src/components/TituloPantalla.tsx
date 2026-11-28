/** Titulo de 20px con un subtitulo gris de una linea. */
export default function TituloPantalla({ titulo, subtitulo }: { titulo: string; subtitulo?: string }) {
  return (
    <div className="mb-[18px]">
      <h1 className="mb-1 text-[20px] font-semibold tracking-tight">{titulo}</h1>
      {subtitulo && <p className="text-[13px] text-muted">{subtitulo}</p>}
    </div>
  );
}
