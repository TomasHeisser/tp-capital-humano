package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.utn.frsf.capitalhumano.dominio.Puesto;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PuestoDAOJpa extends DAOJpa<Puesto, Long> implements PuestoDAO {

    public PuestoDAOJpa() {
        super(Puesto.class);
    }

    @Override
    public List<Puesto> buscarTodos() {
        return consultar("select p from Puesto p join fetch p.empresa where p.fechaBaja is null order by p.nombre", Map.of());
    }

    @Override
    public List<Puesto> buscarPorFiltros(String codigo, String nombre, String empresa) {
        StringBuilder jpql = new StringBuilder("select p from Puesto p join fetch p.empresa e where p.fechaBaja is null");
        Map<String, Object> p = new HashMap<>();
        if (hayTexto(codigo)) {
            jpql.append(" and upper(p.codigo) like :codigo");
            p.put("codigo", contiene(codigo));
        }
        if (hayTexto(nombre)) {
            jpql.append(" and upper(p.nombre) like :nombre");
            p.put("nombre", contiene(nombre));
        }
        if (hayTexto(empresa)) {
            jpql.append(" and upper(e.nombre) like :empresa");
            p.put("empresa", contiene(empresa));
        }
        jpql.append(" order by p.codigo");
        return consultar(jpql.toString(), p);
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return existe("select count(p) from Puesto p where upper(p.codigo) = :c", Map.of("c", codigo.trim().toUpperCase()));
    }

    @Override
    public boolean existeNombre(String nombre) {
        return existe("select count(p) from Puesto p where upper(p.nombre) = :n", Map.of("n", nombre.trim().toUpperCase()));
    }
}
