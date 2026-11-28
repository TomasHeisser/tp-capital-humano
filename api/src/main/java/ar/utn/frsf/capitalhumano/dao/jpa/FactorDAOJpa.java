package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FactorDAOJpa extends DAOJpa<Factor, Long> implements FactorDAO {

    public FactorDAOJpa() {
        super(Factor.class);
    }

    @Override
    public List<Factor> buscarTodos() {
        return consultar("select f from Factor f join fetch f.competencia where f.fechaBaja is null order by f.nombre", Map.of());
    }

    @Override
    public List<Factor> buscarPorFiltros(String nombreComp, String nombreFactor) {
        StringBuilder jpql = new StringBuilder("select f from Factor f join fetch f.competencia c where f.fechaBaja is null");
        Map<String, Object> p = new HashMap<>();
        if (hayTexto(nombreComp)) {
            jpql.append(" and upper(c.nombre) like :comp");
            p.put("comp", contiene(nombreComp));
        }
        if (hayTexto(nombreFactor)) {
            jpql.append(" and upper(f.nombre) like :fac");
            p.put("fac", contiene(nombreFactor));
        }
        jpql.append(" order by f.nombre");
        return consultar(jpql.toString(), p);
    }

    @Override
    public List<Factor> buscarPorCompetencia(Long idCompetencia) {
        return consultar("select f from Factor f join fetch f.competencia c where c.id = :id and f.fechaBaja is null order by f.nroOrden",
                Map.of("id", idCompetencia));
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return existe("select count(f) from Factor f where upper(f.codigo) = :c", Map.of("c", codigo.trim().toUpperCase()));
    }

    @Override
    public boolean existeNombreEnCompetencia(String nombre, Long idCompetencia) {
        return existe("select count(f) from Factor f where upper(f.nombre) = :n and f.competencia.id = :id",
                Map.of("n", nombre.trim().toUpperCase(), "id", idCompetencia));
    }
}
