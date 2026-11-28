package ar.utn.frsf.capitalhumano.reporte;

import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.CriterioReporteDTO;
import ar.utn.frsf.capitalhumano.dto.FilaOrdenMeritoDTO;
import ar.utn.frsf.capitalhumano.dto.OrdenMeritoDTO;
import ar.utn.frsf.capitalhumano.dto.ReporteDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * CU028: orden de merito de un puesto. Clasifica cada cuestionario en tres grupos:
 * completos que alcanzaron el minimo en todas las competencias, completos que no,
 * y el resto agrupado por estado (sin puntaje).
 */
@Component
public class ReporteOrdenMerito implements EstrategiaReporte {

    static final String SIN_EVALUACIONES = "Debe seleccionar al menos una evaluación";

    // orden en que se muestran los grupos del resto
    private static final List<String> ORDEN_ESTADOS = List.of(EstadoCuestionario.EN_PROCESO, EstadoCuestionario.ACTIVO,
            EstadoCuestionario.INCOMPLETO, EstadoCuestionario.SIN_CONTESTAR);

    private final PuestoDAO puestoDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final Supplier<LocalDateTime> reloj;

    @Autowired
    public ReporteOrdenMerito(PuestoDAO puestoDAO, CuestionarioDAO cuestionarioDAO) {
        this(puestoDAO, cuestionarioDAO, LocalDateTime::now);
    }

    // para los tests: hora fija
    ReporteOrdenMerito(PuestoDAO puestoDAO, CuestionarioDAO cuestionarioDAO, Supplier<LocalDateTime> reloj) {
        this.puestoDAO = puestoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.reloj = reloj;
    }

    @Override
    public ReporteDTO generar(CriterioReporteDTO criterio) {
        if (criterio == null || criterio.getIdsEvaluaciones() == null || criterio.getIdsEvaluaciones().isEmpty()) {
            throw new ValidacionException(SIN_EVALUACIONES);
        }
        Puesto puesto = puestoDAO.buscarPorId(criterio.getIdPuesto())
                .orElseThrow(() -> new NoSuchElementException("El puesto no existe"));
        List<Cuestionario> cuestionarios = cuestionarioDAO.buscarPorEvaluaciones(criterio.getIdsEvaluaciones());

        List<FilaOrdenMeritoDTO> ordenMerito = new ArrayList<>();
        List<FilaOrdenMeritoDTO> noAlcanzaronMinimo = new ArrayList<>();
        Map<String, List<FilaOrdenMeritoDTO>> restoPorEstado = new TreeMap<>(Comparator.comparingInt(ORDEN_ESTADOS::indexOf));
        for (Cuestionario c : cuestionarios) {
            // por si mandan una evaluacion de otro puesto
            if (!c.getEvaluacion().getPuesto().getId().equals(puesto.getId())) {
                continue;
            }
            String estado = c.getEstado().getNombre();
            if (EstadoCuestionario.COMPLETO.equals(estado)) {
                // puntajeTotal y alcanzoMinimos trabajan sobre c.getResultados()
                BigDecimal puntaje = puntajeTotal(c);
                if (alcanzoMinimos(c)) {
                    ordenMerito.add(fila(c, estado, puntaje));
                } else {
                    noAlcanzaronMinimo.add(fila(c, estado, puntaje));
                }
            } else {
                restoPorEstado.computeIfAbsent(estado, e -> new ArrayList<>()).add(fila(c, estado, null));
            }
        }
        ordenar(ordenMerito);
        ordenar(noAlcanzaronMinimo);
        for (int i = 0; i < ordenMerito.size(); i++) {
            ordenMerito.get(i).setPosicion(i + 1);
        }
        restoPorEstado.values().forEach(filas -> filas.sort(Comparator.comparing(FilaOrdenMeritoDTO::getApellido)
                .thenComparing(FilaOrdenMeritoDTO::getNombre)));
        return new OrdenMeritoDTO(puesto.getCodigo(), puesto.getNombre(), puesto.getEmpresa().getNombre(), ordenMerito,
                noAlcanzaronMinimo, new LinkedHashMap<>(restoPorEstado), reloj.get());
    }

    /** Llego a la ponderacion minima (>=) en cada una de las competencias evaluadas. */
    private boolean alcanzoMinimos(Cuestionario c) {
        return c.getResultados().stream()
                .allMatch(r -> r.getPuntaje().compareTo(r.getCompetenciaEvaluada().getPonderacionMinima()) >= 0);
    }

    /** Promedio de los puntajes de competencia, con 2 decimales. */
    private BigDecimal puntajeTotal(Cuestionario c) {
        List<Resultado> resultados = c.getResultados();
        if (resultados.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal suma = resultados.stream().map(Resultado::getPuntaje).reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(resultados.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Mayor puntaje primero; si empatan, el que tardo menos (fin - inicio), despues el de
     * menos accesos y por ultimo apellido y nombre.
     */
    private void ordenar(List<FilaOrdenMeritoDTO> filas) {
        filas.sort(Comparator.comparing(FilaOrdenMeritoDTO::getPuntaje, Comparator.reverseOrder())
                .thenComparing(ReporteOrdenMerito::tiempo)
                .thenComparing(FilaOrdenMeritoDTO::getCantidadAccesos, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(FilaOrdenMeritoDTO::getApellido)
                .thenComparing(FilaOrdenMeritoDTO::getNombre));
    }

    private static Duration tiempo(FilaOrdenMeritoDTO f) {
        return f.getFechaInicio() == null || f.getFechaFin() == null
                ? Duration.ofDays(36500) : Duration.between(f.getFechaInicio(), f.getFechaFin());
    }

    private static FilaOrdenMeritoDTO fila(Cuestionario c, String estado, BigDecimal puntaje) {
        Candidato cand = c.getCandidato();
        return new FilaOrdenMeritoDTO(null, cand.getTipoDocumento(), cand.getNroDocumento(), cand.getApellido(), cand.getNombre(),
                estado, puntaje, c.getFechaInicio(), c.getFechaFin(), c.getUltimoIngreso(), c.getCantidadAccesos());
    }
}
