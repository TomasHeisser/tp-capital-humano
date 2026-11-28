package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Factor;

import java.util.List;

/**
 * Acceso a los factores.
 */
public interface FactorDAO extends DAO<Factor, Long> {

    /** Factores activos, ordenados por nombre de factor. */
    List<Factor> buscarPorFiltros(String nombreComp, String nombreFactor);

    /** Factores activos de una competencia, por nro de orden. */
    List<Factor> buscarPorCompetencia(Long idCompetencia);

    boolean existeCodigo(String codigo);

    boolean existeNombreEnCompetencia(String nombre, Long idCompetencia);
}
