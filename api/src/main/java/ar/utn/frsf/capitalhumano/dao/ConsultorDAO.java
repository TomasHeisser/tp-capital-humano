package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Consultor;

import java.util.Optional;

/**
 * Acceso a los consultores (solo se guarda el username).
 */
public interface ConsultorDAO extends DAO<Consultor, Long> {

    Optional<Consultor> buscarPorUsername(String u);
}
