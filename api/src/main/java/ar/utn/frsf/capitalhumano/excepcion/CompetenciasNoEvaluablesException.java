package ar.utn.frsf.capitalhumano.excepcion;

import java.util.List;

/**
 * Alguna competencia del puesto no tiene factores con 2 o mas preguntas (responde 422).
 */
public class CompetenciasNoEvaluablesException extends NegocioException {

    public CompetenciasNoEvaluablesException(String mensaje) {
        super(mensaje);
    }

    public CompetenciasNoEvaluablesException(String mensaje, List<String> errores) {
        super(mensaje, errores);
    }
}
