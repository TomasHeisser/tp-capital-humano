package ar.utn.frsf.capitalhumano.configuracion;

/**
 * Repositorio de configuraciones del sistema: instrucciones y parametros del cuestionario.
 */
public interface RepositorioConfiguracion {

    String getInstrucciones();

    Integer getPreguntasPorBloque();

    Integer getTiempoMaximoMinutos();

    Integer getVigenciaDias();

    Integer getMaxAccesos();
}
