package ar.utn.frsf.capitalhumano.dominio;

/**
 * Se paso del tiempo maximo o de la cantidad de accesos. Es un estado final.
 */
public class EstadoIncompleto extends EstadoCuestionario {

    @Override
    public String getNombre() {
        return INCOMPLETO;
    }
}
