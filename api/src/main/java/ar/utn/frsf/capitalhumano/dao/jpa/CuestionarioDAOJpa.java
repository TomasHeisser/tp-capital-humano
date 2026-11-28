package ar.utn.frsf.capitalhumano.dao.jpa;

import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dominio.Cuestionario;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * El estado se guarda como texto (lo pasa el conversor), asi que las consultas por estado van en SQL.
 */
@Repository
public class CuestionarioDAOJpa extends DAOJpa<Cuestionario, Long> implements CuestionarioDAO {

    public CuestionarioDAOJpa() {
        super(Cuestionario.class);
    }

    @Override
    public Optional<Cuestionario> buscarActivoOEnProceso(Long idCandidato) {
        List<?> lista = em.createNativeQuery("select * from cuestionario where id_candidato = :id"
                        + " and estado in ('ACTIVO', 'EN_PROCESO')", Cuestionario.class)
                .setParameter("id", idCandidato)
                .getResultList();
        return lista.stream().map(Cuestionario.class::cast).findFirst();
    }

    @Override
    public List<Cuestionario> buscarPorEvaluaciones(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return consultar("select c from Cuestionario c join fetch c.candidato where c.evaluacion.id in :ids order by c.id",
                Map.of("ids", ids));
    }

    @Override
    public List<Cuestionario> buscarCompletosPorPuesto(Long idPuesto) {
        return nativa("select c.* from cuestionario c join evaluacion e on e.id = c.id_evaluacion"
                + " where e.id_puesto = :id and c.estado = 'COMPLETO' order by c.fecha_fin", idPuesto);
    }

    @Override
    public List<Cuestionario> buscarPendientes() {
        return nativa("select * from cuestionario where estado in ('ACTIVO', 'EN_PROCESO') order by id", null);
    }

    @Override
    public long contarCandidatosPorPuesto(Long idPuesto) {
        return contar("select count(distinct c.id_candidato) from cuestionario c join evaluacion e on e.id = c.id_evaluacion"
                + " where e.id_puesto = :id", idPuesto);
    }

    @Override
    public long contarCompletosPorPuesto(Long idPuesto) {
        return contar("select count(*) from cuestionario c join evaluacion e on e.id = c.id_evaluacion"
                + " where e.id_puesto = :id and c.estado = 'COMPLETO'", idPuesto);
    }

    private List<Cuestionario> nativa(String sql, Long id) {
        var q = em.createNativeQuery(sql, Cuestionario.class);
        if (id != null) {
            q.setParameter("id", id);
        }
        return ((List<?>) q.getResultList()).stream().map(Cuestionario.class::cast).toList();
    }

    private long contar(String sql, Long id) {
        Number n = (Number) em.createNativeQuery(sql).setParameter("id", id).getSingleResult();
        return n.longValue();
    }
}
