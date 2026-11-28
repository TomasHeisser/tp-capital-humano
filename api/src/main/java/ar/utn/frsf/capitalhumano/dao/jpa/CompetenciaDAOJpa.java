package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.CompetenciaDAO;
import ar.utn.frsf.capitalhumano.dominio.Competencia;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class CompetenciaDAOJpa extends DAOJpa<Competencia, Long> implements CompetenciaDAO {

    public CompetenciaDAOJpa() {
        super(Competencia.class);
    }

    // solo las activas, que son las que se pueden elegir
    @Override
    public List<Competencia> buscarTodos() {
        return consultar("select c from Competencia c where c.fechaBaja is null order by c.nombre", Map.of());
    }

    @Override
    public List<Competencia> buscarPorNombre(String nombre) {
        if (!hayTexto(nombre)) {
            return buscarTodos();
        }
        return consultar("select c from Competencia c where c.fechaBaja is null and upper(c.nombre) like :n order by c.nombre",
                Map.of("n", contiene(nombre)));
    }

    @Override
    public boolean existeCodigo(String codigo) {
        return existe("select count(c) from Competencia c where upper(c.codigo) = :c", Map.of("c", codigo.trim().toUpperCase()));
    }

    @Override
    public boolean existeNombre(String nombre) {
        return existe("select count(c) from Competencia c where upper(c.nombre) = :n", Map.of("n", nombre.trim().toUpperCase()));
    }
}
