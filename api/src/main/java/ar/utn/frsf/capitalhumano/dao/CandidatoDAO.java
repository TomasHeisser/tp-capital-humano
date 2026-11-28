package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Candidato;
import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a los candidatos.
 */
public interface CandidatoDAO extends DAO<Candidato, Long> {

    Optional<Candidato> buscarPorNroCandidato(Long nro);

    /** Candidato sin baja con ese documento. */
    Optional<Candidato> buscarPorDocumento(TipoDocumento tipo, String nro);

    /** Candidatos sin baja; apellido y nombre con "contiene", los filtros nulos no se usan. */
    List<Candidato> buscarPorFiltros(String apellido, String nombre, Long nro);
}
