package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.RegistroEjecucionDAO;
import ar.utn.frsf.capitalhumano.dominio.RegistroEjecucion;
import org.springframework.stereotype.Repository;

@Repository
public class RegistroEjecucionDAOJpa extends DAOJpa<RegistroEjecucion, Long> implements RegistroEjecucionDAO {

    public RegistroEjecucionDAOJpa() {
        super(RegistroEjecucion.class);
    }
}
