package ar.utn.frsf.capitalhumano.dao;

import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;

import java.util.List;

/**
 * Acceso a los conjuntos de opciones de respuesta. buscarTodos devuelve solo los activos.
 */
public interface OpcionRespuestaDAO extends DAO<OpcionRespuesta, Long> {

    /** Conjuntos activos cuyo nombre contiene el texto (sin texto: todos). */
    List<OpcionRespuesta> buscarPorNombre(String nombre);

    boolean existeNombre(String nombre);
}
