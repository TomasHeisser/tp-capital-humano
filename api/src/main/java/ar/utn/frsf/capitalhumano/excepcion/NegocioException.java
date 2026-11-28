package ar.utn.frsf.capitalhumano.excepcion;

import java.util.List;

/**
 * Error de una regla de negocio. Lleva un mensaje general y la lista de errores
 * (los gestores juntan todos antes de tirarla, asi la pantalla los muestra juntos).
 */
public class NegocioException extends RuntimeException {

    private final List<String> errores;

    public NegocioException(String mensaje) {
        this(mensaje, List.of());
    }

    public NegocioException(String mensaje, List<String> errores) {
        super(mensaje);
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
