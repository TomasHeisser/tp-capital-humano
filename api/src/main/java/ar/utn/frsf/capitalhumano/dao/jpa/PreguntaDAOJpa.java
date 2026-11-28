package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.PreguntaDAO;
import ar.utn.frsf.capitalhumano.dominio.Pregunta;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PreguntaDAOJpa extends DAOJpa<Pregunta, Long> implements PreguntaDAO {

    public PreguntaDAOJpa() {
        super(Pregunta.class);
    }

    @Override
    public List<Pregunta> buscarPorFiltros(Long idComp, Long idFactor, String nombre) {
        StringBuilder jpql = new StringBuilder(
                "select p from Pregunta p join fetch p.factor f join fetch f.competencia c where p.fechaBaja is null");
        Map<String, Object> par = new HashMap<>();
        if (idComp != null) {
            jpql.append(" and c.id = :comp");
            par.put("comp", idComp);
        }
        if (idFactor != null) {
            jpql.append(" and f.id = :factor");
            par.put("factor", idFactor);
        }
        if (hayTexto(nombre)) {
            jpql.append(" and upper(p.nombre) like :nombre");
            par.put("nombre", contiene(nombre));
        }
        jpql.append(" order by c.nombre, f.nroOrden, p.nombre");
        return consultar(jpql.toString(), par);
    }

    @Override
    public List<Pregunta> buscarCoincidencias(Long idFactor, String nombre, String enunciado, Long idExcluir) {
        StringBuilder jpql = new StringBuilder("select p from Pregunta p where p.factor.id = :factor and p.fechaBaja is null"
                + " and (upper(p.nombre) = :nombre or upper(p.enunciado) = :enunciado)");
        Map<String, Object> par = new HashMap<>();
        par.put("factor", idFactor);
        par.put("nombre", nombre == null ? "" : nombre.trim().toUpperCase());
        par.put("enunciado", enunciado == null ? "" : enunciado.trim().toUpperCase());
        if (idExcluir != null) {
            jpql.append(" and p.id <> :excluir");
            par.put("excluir", idExcluir);
        }
        return consultar(jpql.toString(), par);
    }
}
