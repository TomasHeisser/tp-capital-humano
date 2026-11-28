package ar.utn.frsf.capitalhumano.reporte;

import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.CriterioReporteDTO;
import ar.utn.frsf.capitalhumano.dto.FilaComparativoDTO;
import ar.utn.frsf.capitalhumano.dto.ReporteComparativoDTO;
import ar.utn.frsf.capitalhumano.dto.ReporteDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * CU027: compara, competencia por competencia, la ponderacion que pide el puesto con el
 * puntaje que saco el candidato en su cuestionario completo.
 */
@Component
public class ReporteComparativo implements EstrategiaReporte {

    static final String SIN_CANDIDATO = "Se debe seleccionar al menos un candidato";

    private final CuestionarioDAO cuestionarioDAO;
    private final Supplier<LocalDateTime> reloj;

    @Autowired
    public ReporteComparativo(CuestionarioDAO cuestionarioDAO) {
        this(cuestionarioDAO, LocalDateTime::now);
    }

    // para los tests: hora fija
    ReporteComparativo(CuestionarioDAO cuestionarioDAO, Supplier<LocalDateTime> reloj) {
        this.cuestionarioDAO = cuestionarioDAO;
        this.reloj = reloj;
    }

    @Override
    public ReporteDTO generar(CriterioReporteDTO criterio) {
        if (criterio == null || criterio.getIdCuestionario() == null) {
            throw new ValidacionException(SIN_CANDIDATO);
        }
        Cuestionario c = cuestionarioDAO.buscarPorId(criterio.getIdCuestionario())
                .filter(x -> EstadoCuestionario.COMPLETO.equals(x.getEstado().getNombre()))
                .orElseThrow(() -> new NoSuchElementException("El candidato no tiene un cuestionario completo"));
        Puesto puesto = c.getEvaluacion().getPuesto();
        List<FilaComparativoDTO> filas = c.getResultados().stream()
                .sorted(Comparator.comparing((Resultado r) -> r.getCompetenciaEvaluada().getId()))
                .map(r -> new FilaComparativoDTO(r.getCompetenciaEvaluada().getNombre(),
                        r.getCompetenciaEvaluada().getPonderacionMinima(), r.getPuntaje()))
                .toList();
        Candidato cand = c.getCandidato();
        return new ReporteComparativoDTO(puesto.getCodigo(), puesto.getNombre(), puesto.getEmpresa().getNombre(),
                cand.getApellido() + ", " + cand.getNombre(), filas, reloj.get());
    }
}
