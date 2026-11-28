package ar.utn.frsf.capitalhumano.excepcion;

import java.util.List;

/**
 * El candidato no respondio todas las preguntas del bloque (responde 400).
 */
public class RespuestasIncompletasException extends NegocioException {

    public RespuestasIncompletasException(String mensaje) {
        super(mensaje);
    }

    public RespuestasIncompletasException(String mensaje, List<String> errores) {
        super(mensaje, errores);
    }
}
