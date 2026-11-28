package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.DAO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base de todos los DAO con JPA: resuelve lo comun usando el EntityManager.
 * Las transacciones las abren los gestores.
 *
 * @param <T>  entidad
 * @param <ID> tipo de la clave
 */
public abstract class DAOJpa<T, ID> implements DAO<T, ID> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> clase;

    protected DAOJpa(Class<T> clase) {
        this.clase = clase;
    }

    @Override
    public Optional<T> buscarPorId(ID id) {
        return id == null ? Optional.empty() : Optional.ofNullable(em.find(clase, id));
    }

    @Override
    public List<T> buscarTodos() {
        return em.createQuery("select e from " + clase.getSimpleName() + " e", clase).getResultList();
    }

    // el flush hace que si falla una restriccion de la base salte aca y no al final
    @Override
    public boolean guardar(T e) {
        em.persist(e);
        em.flush();
        return true;
    }

    @Override
    public boolean actualizar(T e) {
        em.merge(e);
        em.flush();
        return true;
    }

    // ---- ayudas para armar las busquedas con filtros opcionales ----

    /** Patron para el criterio "contiene" sin distinguir mayusculas. */
    protected static String contiene(String texto) {
        return "%" + texto.trim().toUpperCase() + "%";
    }

    protected static boolean hayTexto(String texto) {
        return texto != null && !texto.isBlank();
    }

    protected List<T> consultar(String jpql, Map<String, Object> parametros) {
        TypedQuery<T> q = em.createQuery(jpql, clase);
        parametros.forEach(q::setParameter);
        return q.getResultList();
    }

    protected boolean existe(String jpqlCount, Map<String, Object> parametros) {
        var q = em.createQuery(jpqlCount, Long.class);
        parametros.forEach(q::setParameter);
        return q.getSingleResult() > 0;
    }
}
