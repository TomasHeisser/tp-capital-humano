package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Cuestionario;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los cuestionarios.
 */
public interface CuestionarioDAO extends DAO<Cuestionario, Long> {

    /** Cuestionario Activo o En proceso del candidato (puede tener uno solo). */
    Optional<Cuestionario> buscarActivoOEnProceso(Long idCandidato);

    List<Cuestionario> buscarPorEvaluaciones(List<Long> ids);

    List<Cuestionario> buscarCompletosPorPuesto(Long idPuesto);

    /** Los que todavia pueden vencer: Activos y En proceso. */
    List<Cuestionario> buscarPendientes();

    long contarCandidatosPorPuesto(Long idPuesto);

    long contarCompletosPorPuesto(Long idPuesto);
}
