package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.EmpresaDAO;
import ar.utn.frsf.capitalhumano.dominio.Empresa;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class EmpresaDAOJpa extends DAOJpa<Empresa, Long> implements EmpresaDAO {

    public EmpresaDAOJpa() {
        super(Empresa.class);
    }

    @Override
    public List<Empresa> buscarTodos() {
        return consultar("select e from Empresa e order by e.nombre", Map.of());
    }
}
