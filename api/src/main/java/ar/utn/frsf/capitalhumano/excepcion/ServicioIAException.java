package ar.utn.frsf.capitalhumano.excepcion;

import java.util.List;

/**
 * No se pudo hablar con el servicio de IA (responde 503).
 */
public class ServicioIAException extends NegocioException {

    public ServicioIAException(String mensaje) {
        super(mensaje);
    }

    public ServicioIAException(String mensaje, List<String> errores) {
        super(mensaje, errores);
    }
}
