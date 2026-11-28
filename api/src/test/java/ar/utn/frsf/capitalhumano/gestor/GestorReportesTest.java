package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dao.EvaluacionDAO;
import ar.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import ar.utn.frsf.capitalhumano.reporte.ReporteComparativo;
import ar.utn.frsf.capitalhumano.reporte.ReporteOrdenMerito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorReportesTest {

    @Mock
    private PuestoDAO puestoDAO;
    @Mock
    private EvaluacionDAO evaluacionDAO;
    @Mock
    private CuestionarioDAO cuestionarioDAO;
    @Mock
    private ReporteOrdenMerito ordenMerito;
    @Mock
    private ReporteComparativo comparativo;

    private GestorReportes gestor;
    private Puesto puesto;

    @BeforeEach
    void setUp() {
        gestor = new GestorReportes(puestoDAO, evaluacionDAO, cuestionarioDAO, ordenMerito, comparativo);
        puesto = new Puesto("DEV-JR-01", "DESARROLLADOR JAVA JUNIOR", null, new Empresa("TECNOSUR S.A."));
        puesto.setId(1L);
    }

    @Test
    void buscarPuestosSinCriterioNoConsulta() {
        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.buscarPuestos(new FiltroPuestoDTO(" ", null, "")));
        assertEquals("Debe ingresar al menos un criterio de búsqueda", e.getMessage());
        assertThrows(ValidacionException.class, () -> gestor.buscarPuestos(null));
        verifyNoInteractions(puestoDAO, cuestionarioDAO);
    }

    @Test
    void buscarPuestosConCandidatosYEvaluacionesCompletas() {
        when(puestoDAO.buscarPorFiltros(null, "DESARROLLADOR", null)).thenReturn(List.of(puesto));
        when(cuestionarioDAO.contarCandidatosPorPuesto(1L)).thenReturn(6L);
        when(cuestionarioDAO.contarCompletosPorPuesto(1L)).thenReturn(3L);

        List<PuestoEstadisticaDTO> r = gestor.buscarPuestos(new FiltroPuestoDTO(null, "DESARROLLADOR", null));

        assertEquals(List.of(new PuestoEstadisticaDTO(1L, "DEV-JR-01", "DESARROLLADOR JAVA JUNIOR", "TECNOSUR S.A.", 6, 3)), r);
    }

    @Test
    void listarEvaluacionesConFechaYCantidadDeCandidatos() {
        Evaluacion ev = new Evaluacion(LocalDateTime.of(2026, 11, 12, 9, 15), puesto, new Consultor("JPEREZ"));
        ev.setId(4L);
        ev.agregarCuestionario(new Cuestionario(new Candidato(), "AAAA1111", 5, 120, 7, 5, ev.getFechaCreacion()));
        ev.agregarCuestionario(new Cuestionario(new Candidato(), "BBBB2222", 5, 120, 7, 5, ev.getFechaCreacion()));
        when(evaluacionDAO.buscarPorPuesto(1L)).thenReturn(List.of(ev));

        List<EvaluacionDTO> r = gestor.listarEvaluaciones(1L);

        assertEquals(List.of(new EvaluacionDTO(4L, ev.getFechaCreacion(), "DESARROLLADOR JAVA JUNIOR", "TECNOSUR S.A.", 2)), r);
        when(evaluacionDAO.buscarPorPuesto(2L)).thenReturn(List.of());
        assertTrue(gestor.listarEvaluaciones(2L).isEmpty(), "6.A: el puesto no posee evaluaciones");
    }

    @Test
    void listarEvaluadosConLosCompletos() {
        Candidato gomez = new Candidato();
        gomez.setApellido("GÓMEZ");
        gomez.setNombre("MARÍA");
        gomez.setTipoDocumento(TipoDocumento.DNI);
        gomez.setNroDocumento("30123456");
        gomez.setNroCandidato(1001L);
        Cuestionario c = new Cuestionario(gomez, "AAAA1111", 5, 120, 7, 5, LocalDateTime.of(2026, 11, 12, 9, 15));
        c.setId(1L);
        c.setFechaFin(LocalDateTime.of(2026, 11, 20, 18, 40));
        when(cuestionarioDAO.buscarCompletosPorPuesto(1L)).thenReturn(List.of(c));

        List<CandidatoEvaluadoDTO> r = gestor.listarEvaluados(1L);

        assertEquals(List.of(new CandidatoEvaluadoDTO(1L, "GÓMEZ", "MARÍA", TipoDocumento.DNI, "30123456", 1001L,
                LocalDateTime.of(2026, 11, 20, 18, 40))), r);
    }

    @Test
    void emitirReporteEligeLaEstrategiaSegunElTipo() {
        CriterioReporteDTO criterio = new CriterioReporteDTO(1L, List.of(1L), null);
        OrdenMeritoDTO om = new OrdenMeritoDTO();
        ReporteComparativoDTO rc = new ReporteComparativoDTO();
        when(ordenMerito.generar(criterio)).thenReturn(om);
        when(comparativo.generar(criterio)).thenReturn(rc);

        assertSame(om, gestor.emitirReporte(TipoReporte.ORDEN_MERITO, criterio));
        assertSame(rc, gestor.emitirReporte(TipoReporte.COMPARATIVO, criterio));
        verify(ordenMerito).generar(criterio);
        verify(comparativo).generar(criterio);
        assertThrows(ValidacionException.class, () -> gestor.emitirReporte(null, criterio));
    }
}
