package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.OpcionRespuestaDAO;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OpcionRespuestaDAOJpa extends DAOJpa<OpcionRespuesta, Long> implements OpcionRespuestaDAO {

    public OpcionRespuestaDAOJpa() {
        super(OpcionRespuesta.class);
    }

    // trae tambien los items, que siempre se muestran con el conjunto
    @Override
    public List<OpcionRespuesta> buscarTodos() {
        return consultar("select distinct o from OpcionRespuesta o left join fetch o.items where o.fechaBaja is null order by o.nombre",
                Map.of());
    }

    @Override
    public List<OpcionRespuesta> buscarPorNombre(String nombre) {
        if (!hayTexto(nombre)) {
            return buscarTodos();
        }
        return consultar("select distinct o from OpcionRespuesta o left join fetch o.items"
                + " where o.fechaBaja is null and upper(o.nombre) like :n order by o.nombre", Map.of("n", contiene(nombre)));
    }

    @Override
    public boolean existeNombre(String nombre) {
        return existe("select count(o) from OpcionRespuesta o where upper(o.nombre) = :n", Map.of("n", nombre.trim().toUpperCase()));
    }
}
