package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.utn.frsf.capitalhumano.dominio.Consultor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public class ConsultorDAOJpa extends DAOJpa<Consultor, Long> implements ConsultorDAO {

    public ConsultorDAOJpa() {
        super(Consultor.class);
    }

    @Override
    public Optional<Consultor> buscarPorUsername(String u) {
        return consultar("select c from Consultor c where upper(c.username) = :u",
                Map.of("u", u.trim().toUpperCase())).stream().findFirst();
    }
}
