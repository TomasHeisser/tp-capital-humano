package ar.utn.frsf.capitalhumano.dominio;

/**
 * Vencio la vigencia sin que lo empiece. Es un estado final.
 */
public class EstadoSinContestar extends EstadoCuestionario {

    @Override
    public String getNombre() {
        return SIN_CONTESTAR;
    }
}
