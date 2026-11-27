package ar.utn.frsf.capitalhumano.dao;

import java.util.List;
import java.util.Optional;

/**
 * DAO generico: las operaciones que tienen todas las entidades.
 *
 * @param <T>  entidad
 * @param <ID> tipo de la clave
 */
public interface DAO<T, ID> {

    Optional<T> buscarPorId(ID id);

    List<T> buscarTodos();

    boolean guardar(T e);

    boolean actualizar(T e);
}
