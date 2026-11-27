package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.Puesto;

import java.util.List;

/**
 * Acceso a los puestos.
 */
public interface PuestoDAO extends DAO<Puesto, Long> {

    /** Puestos activos que contienen los textos dados (los nulos no filtran). */
    List<Puesto> buscarPorFiltros(String codigo, String nombre, String empresa);

    /** Incluye los dados de baja. */
    boolean existeCodigo(String codigo);

    /** Incluye los dados de baja. */
    boolean existeNombre(String nombre);
}
