package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.CandidatoDAO;
import ar.utn.frsf.capitalhumano.dominio.Candidato;
import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class CandidatoDAOJpa extends DAOJpa<Candidato, Long> implements CandidatoDAO {

    public CandidatoDAOJpa() {
        super(Candidato.class);
    }

    @Override
    public Optional<Candidato> buscarPorNroCandidato(Long nro) {
        return consultar("select c from Candidato c where c.nroCandidato = :nro", Map.of("nro", nro))
                .stream().findFirst();
    }

    @Override
    public Optional<Candidato> buscarPorDocumento(TipoDocumento tipo, String nro) {
        return consultar("select c from Candidato c where c.tipoDocumento = :tipo and upper(c.nroDocumento) = :nro"
                        + " and c.fechaBaja is null",
                Map.of("tipo", tipo, "nro", nro.trim().toUpperCase())).stream().findFirst();
    }

    @Override
    public List<Candidato> buscarPorFiltros(String apellido, String nombre, Long nro) {
        StringBuilder jpql = new StringBuilder("select c from Candidato c where c.fechaBaja is null");
        Map<String, Object> p = new HashMap<>();
        if (hayTexto(apellido)) {
            jpql.append(" and upper(c.apellido) like :apellido");
            p.put("apellido", contiene(apellido));
        }
        if (hayTexto(nombre)) {
            jpql.append(" and upper(c.nombre) like :nombre");
            p.put("nombre", contiene(nombre));
        }
        if (nro != null) {
            jpql.append(" and c.nroCandidato = :nro");
            p.put("nro", nro);
        }
        jpql.append(" order by c.apellido, c.nombre");
        return consultar(jpql.toString(), p);
    }
}
