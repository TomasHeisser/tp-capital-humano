package ar.utn.frsf.capitalhumano.dominio;

/**
 * Respondio todos los bloques. Ya no se puede volver a ingresar.
 */
public class EstadoCompleto extends EstadoCuestionario {

    @Override
    public String getNombre() {
        return COMPLETO;
    }
}
