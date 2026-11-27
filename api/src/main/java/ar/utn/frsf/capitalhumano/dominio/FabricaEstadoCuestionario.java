package ar.utn.frsf.capitalhumano.dominio;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory Method de los estados: a partir del nombre guardado en cuestionario.estado
 * (o del que pide una transicion) arma el objeto estado que corresponde.
 */
public class FabricaEstadoCuestionario {

    private static final Map<String, Supplier<EstadoCuestionario>> ESTADOS = Map.of(
            EstadoCuestionario.ACTIVO, EstadoActivo::new,
            EstadoCuestionario.EN_PROCESO, EstadoEnProceso::new,
            EstadoCuestionario.COMPLETO, EstadoCompleto::new,
            EstadoCuestionario.INCOMPLETO, EstadoIncompleto::new,
            EstadoCuestionario.SIN_CONTESTAR, EstadoSinContestar::new);

    public EstadoCuestionario crearEstado(String nombre) {
        Supplier<EstadoCuestionario> creador = ESTADOS.get(nombre);
        if (creador == null) {
            throw new IllegalArgumentException("Estado de cuestionario desconocido: " + nombre);
        }
        return creador.get();
    }
}
