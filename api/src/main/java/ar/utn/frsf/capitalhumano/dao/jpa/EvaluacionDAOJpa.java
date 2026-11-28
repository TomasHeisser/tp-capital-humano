package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.EvaluacionDAO;
import ar.utn.frsf.capitalhumano.dominio.Evaluacion;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class EvaluacionDAOJpa extends DAOJpa<Evaluacion, Long> implements EvaluacionDAO {

    public EvaluacionDAOJpa() {
        super(Evaluacion.class);
    }

    @Override
    public List<Evaluacion> buscarPorPuesto(Long idPuesto) {
        return consultar("select e from Evaluacion e where e.puesto.id = :id order by e.fechaCreacion", Map.of("id", idPuesto));
    }
}
