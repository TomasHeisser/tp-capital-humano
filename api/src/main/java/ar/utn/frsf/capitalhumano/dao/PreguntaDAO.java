package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Pregunta;

import java.util.List;

/**
 * Acceso a las preguntas.
 */
public interface PreguntaDAO extends DAO<Pregunta, Long> {

    /** Preguntas activas; el nombre con "contiene" y los filtros nulos no se usan. */
    List<Pregunta> buscarPorFiltros(Long idComp, Long idFactor, String nombre);

    /** Preguntas activas del factor con el mismo nombre o el mismo enunciado (salvo idExcluir). */
    List<Pregunta> buscarCoincidencias(Long idFactor, String nombre, String enunciado, Long idExcluir);
}
