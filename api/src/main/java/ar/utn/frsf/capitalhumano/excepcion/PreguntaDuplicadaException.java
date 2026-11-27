package ar.utn.frsf.capitalhumano.excepcion;

import java.util.List;

/**
 * Ya hay una pregunta activa con ese nombre o enunciado en el factor (responde 409).
 */
public class PreguntaDuplicadaException extends NegocioException {

    public PreguntaDuplicadaException(String mensaje) {
        super(mensaje);
    }

    public PreguntaDuplicadaException(String mensaje, List<String> errores) {
        super(mensaje, errores);
    }
}
