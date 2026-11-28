package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Competencia;

import java.util.List;

/**
 * Acceso a las competencias. buscarTodos devuelve solo las activas.
 */
public interface CompetenciaDAO extends DAO<Competencia, Long> {

    /** Competencias activas cuyo nombre contiene el texto (sin texto: todas). */
    List<Competencia> buscarPorNombre(String nombre);

    boolean existeCodigo(String codigo);

    boolean existeNombre(String nombre);
}
