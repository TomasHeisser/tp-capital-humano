package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.RegistroAuditoriaDAO;
import ar.utn.frsf.capitalhumano.dominio.RegistroAuditoria;
import org.springframework.stereotype.Repository;

@Repository
public class RegistroAuditoriaDAOJpa extends DAOJpa<RegistroAuditoria, Long> implements RegistroAuditoriaDAO {

    public RegistroAuditoriaDAOJpa() {
        super(RegistroAuditoria.class);
    }
}
