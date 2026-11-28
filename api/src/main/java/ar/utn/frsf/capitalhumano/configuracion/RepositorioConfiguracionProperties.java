package ar.utn.frsf.capitalhumano.configuracion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Implementacion que lee los parametros de application.properties (prefijo cuestionario.).
 */
@Component
public class RepositorioConfiguracionProperties implements RepositorioConfiguracion {

    private final String instrucciones;
    private final Integer preguntasPorBloque;
    private final Integer tiempoMaximoMinutos;
    private final Integer vigenciaDias;
    private final Integer maxAccesos;

    public RepositorioConfiguracionProperties(@Value("${cuestionario.instrucciones}") String instrucciones,
                                              @Value("${cuestionario.preguntas-por-bloque}") Integer preguntasPorBloque,
                                              @Value("${cuestionario.tiempo-maximo-minutos}") Integer tiempoMaximoMinutos,
                                              @Value("${cuestionario.vigencia-dias}") Integer vigenciaDias,
                                              @Value("${cuestionario.max-accesos}") Integer maxAccesos) {
        this.instrucciones = instrucciones;
        this.preguntasPorBloque = preguntasPorBloque;
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
        this.vigenciaDias = vigenciaDias;
        this.maxAccesos = maxAccesos;
    }

    @Override
    public String getInstrucciones() {
        return instrucciones;
    }

    @Override
    public Integer getPreguntasPorBloque() {
        return preguntasPorBloque;
    }

    @Override
    public Integer getTiempoMaximoMinutos() {
        return tiempoMaximoMinutos;
    }

    @Override
    public Integer getVigenciaDias() {
        return vigenciaDias;
    }

    @Override
    public Integer getMaxAccesos() {
        return maxAccesos;
    }
}
