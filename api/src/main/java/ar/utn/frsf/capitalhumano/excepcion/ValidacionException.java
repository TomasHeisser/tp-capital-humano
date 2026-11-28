package ar.utn.frsf.capitalhumano.excepcion;

import java.util.List;

/**
 * Faltan datos, sobran caracteres o un valor no es valido (responde 400).
 */
public class ValidacionException extends NegocioException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    public ValidacionException(String mensaje, List<String> errores) {
        super(mensaje, errores);
    }
}
