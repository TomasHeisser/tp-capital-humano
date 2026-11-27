package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Evaluacion;

import java.util.List;

/**
 * Acceso a las evaluaciones.
 */
public interface EvaluacionDAO extends DAO<Evaluacion, Long> {

    List<Evaluacion> buscarPorPuesto(Long idPuesto);
}
