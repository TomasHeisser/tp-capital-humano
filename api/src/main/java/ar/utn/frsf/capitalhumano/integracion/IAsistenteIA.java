package ar.utn.frsf.capitalhumano.integracion;

import ar.utn.frsf.capitalhumano.dominio.Competencia;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import ar.utn.frsf.capitalhumano.dto.PreguntaDTO;

import java.util.List;

/**
 * Lo que el sistema necesita de un asistente de IA (patron Adapter): proponer una pregunta
 * para un factor. Hay dos implementaciones, Gemini y una simulada, y se elige por configuracion.
 */
public interface IAsistenteIA {

    /**
     * Propone nombre, enunciado, descripcion y conjunto de opciones de una pregunta.
     * Las ponderaciones no las propone: las carga el consultor.
     *
     * @throws ar.utn.frsf.capitalhumano.excepcion.ServicioIAException si no se pudo hablar con el servicio
     */
    PreguntaDTO generarPregunta(Competencia comp, Factor factor, List<OpcionRespuesta> opciones);
}
