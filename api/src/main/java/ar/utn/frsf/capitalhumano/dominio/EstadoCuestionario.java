package ar.utn.frsf.capitalhumano.dominio;

import java.time.LocalDateTime;

/**
 * Estado del cuestionario (patron State). Por defecto no deja hacer nada:
 * cada estado concreto pisa solo lo que le corresponde.
 */
public abstract class EstadoCuestionario {

    public static final String ACTIVO = "ACTIVO";
    public static final String EN_PROCESO = "EN_PROCESO";
    public static final String COMPLETO = "COMPLETO";
    public static final String INCOMPLETO = "INCOMPLETO";
    public static final String SIN_CONTESTAR = "SIN_CONTESTAR";

    /** Devuelve true si el candidato puede seguir con el cuestionario. */
    public boolean registrarAcceso(Cuestionario c, LocalDateTime ahora) {
        return false;
    }

    public void iniciar(Cuestionario c, LocalDateTime ahora) {
        throw new IllegalStateException("No se puede iniciar un cuestionario en estado " + getNombre());
    }

    public void completar(Cuestionario c, LocalDateTime ahora) {
        throw new IllegalStateException("No se puede completar un cuestionario en estado " + getNombre());
    }

    /** Devuelve true si el cuestionario vencio y cambio de estado. */
    public boolean verificarVencimiento(Cuestionario c, LocalDateTime ahora) {
        return false;
    }

    public abstract String getNombre();

    @Override
    public String toString() {
        return getNombre();
    }
}
