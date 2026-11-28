package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dao.EvaluacionDAO;
import ar.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.utn.frsf.capitalhumano.dominio.Candidato;
import ar.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.utn.frsf.capitalhumano.dominio.Evaluacion;
import ar.utn.frsf.capitalhumano.dominio.Puesto;
import ar.utn.frsf.capitalhumano.dominio.TipoReporte;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import ar.utn.frsf.capitalhumano.reporte.EstrategiaReporte;
import ar.utn.frsf.capitalhumano.reporte.ReporteComparativo;
import ar.utn.frsf.capitalhumano.reporte.ReporteOrdenMerito;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * CU027 Reporte comparativo y CU028 Orden de merito. Es el contexto del patron Strategy:
 * busca los datos para elegir y le pide el reporte a la estrategia que corresponde al tipo.
 */
@Service
public class GestorReportes {

    static final String SIN_CRITERIO = "Debe ingresar al menos un criterio de búsqueda";

    private final Map<TipoReporte, EstrategiaReporte> estrategias = new EnumMap<>(TipoReporte.class);
    private final PuestoDAO puestoDAO;
    private final EvaluacionDAO evaluacionDAO;
    private final CuestionarioDAO cuestionarioDAO;

    public GestorReportes(PuestoDAO puestoDAO, EvaluacionDAO evaluacionDAO, CuestionarioDAO cuestionarioDAO,
                          ReporteOrdenMerito ordenMerito, ReporteComparativo comparativo) {
        this.puestoDAO = puestoDAO;
        this.evaluacionDAO = evaluacionDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        estrategias.put(TipoReporte.ORDEN_MERITO, ordenMerito);
        estrategias.put(TipoReporte.COMPARATIVO, comparativo);
    }

    /** CU028 pasos 3 y 4: puestos con cantidad de candidatos distintos y de cuestionarios completos. */
    @Transactional(readOnly = true)
    public List<PuestoEstadisticaDTO> buscarPuestos(FiltroPuestoDTO f) {
        boolean hayCriterio = f != null && Stream.of(f.getCodigo(), f.getNombre(), f.getEmpresa())
                .anyMatch(s -> s != null && !s.isBlank());
        if (!hayCriterio) {
            throw new ValidacionException(SIN_CRITERIO);
        }
        List<Puesto> puestos = puestoDAO.buscarPorFiltros(f.getCodigo(), f.getNombre(), f.getEmpresa());
        List<PuestoEstadisticaDTO> resultado = new ArrayList<>();
        for (Puesto p : puestos) {
            long cantidadCandidatos = cuestionarioDAO.contarCandidatosPorPuesto(p.getId());
            long evaluacionesCompletas = cuestionarioDAO.contarCompletosPorPuesto(p.getId());
            resultado.add(new PuestoEstadisticaDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getEmpresa().getNombre(),
                    cantidadCandidatos, evaluacionesCompletas));
        }
        return resultado;
    }

    /** CU028 paso 5: las evaluaciones del puesto (vacia = 6.A, el puesto no posee evaluaciones). */
    @Transactional(readOnly = true)
    public List<EvaluacionDTO> listarEvaluaciones(Long idPuesto) {
        List<Evaluacion> evaluaciones = evaluacionDAO.buscarPorPuesto(idPuesto);
        List<EvaluacionDTO> resultado = new ArrayList<>();
        for (Evaluacion e : evaluaciones) {
            resultado.add(GestorEvaluaciones.evaluacionDTO(e));
        }
        return resultado;
    }

    /** CU027: candidatos que completaron el cuestionario en el puesto. */
    @Transactional(readOnly = true)
    public List<CandidatoEvaluadoDTO> listarEvaluados(Long idPuesto) {
        List<CandidatoEvaluadoDTO> resultado = new ArrayList<>();
        for (Cuestionario c : cuestionarioDAO.buscarCompletosPorPuesto(idPuesto)) {
            Candidato cand = c.getCandidato();
            resultado.add(new CandidatoEvaluadoDTO(c.getId(), cand.getApellido(), cand.getNombre(), cand.getTipoDocumento(),
                    cand.getNroDocumento(), cand.getNroCandidato(), c.getFechaFin()));
        }
        return resultado;
    }

    /** Elige la estrategia segun el tipo (Strategy) y le pide el reporte. */
    @Transactional(readOnly = true)
    public ReporteDTO emitirReporte(TipoReporte tipo, CriterioReporteDTO criterio) {
        EstrategiaReporte estrategia = estrategias.get(tipo);
        if (estrategia == null) {
            throw new ValidacionException("Tipo de reporte desconocido");
        }
        return estrategia.generar(criterio);
    }
}
