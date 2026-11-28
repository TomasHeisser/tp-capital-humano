package ar.utn.frsf.capitalhumano.reporte;

import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.CriterioReporteDTO;
import ar.utn.frsf.capitalhumano.dto.FilaOrdenMeritoDTO;
import ar.utn.frsf.capitalhumano.dto.OrdenMeritoDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteOrdenMeritoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 27, 11, 20);

    private PuestoDAO puestoDAO;
    private CuestionarioDAO cuestionarioDAO;
    private ReporteOrdenMerito reporte;
    private Datos d;
    private final List<Cuestionario> cuestionarios = new ArrayList<>();

    @BeforeEach
    void setUp() {
        puestoDAO = mock(PuestoDAO.class);
        cuestionarioDAO = mock(CuestionarioDAO.class);
        reporte = new ReporteOrdenMerito(puestoDAO, cuestionarioDAO, () -> AHORA);
        d = new Datos();
        when(puestoDAO.buscarPorId(1L)).thenReturn(Optional.of(d.puesto));
        when(cuestionarioDAO.buscarPorEvaluaciones(List.of(1L, 2L))).thenReturn(cuestionarios);
    }

    private OrdenMeritoDTO emitir() {
        return (OrdenMeritoDTO) reporte.generar(new CriterioReporteDTO(1L, List.of(1L, 2L), null));
    }

    private static List<String> apellidos(List<FilaOrdenMeritoDTO> filas) {
        return filas.stream().map(FilaOrdenMeritoDTO::getApellido).toList();
    }

    @Test
    void conLosDatosDelSeedFernandezQuedaFueraPorNoLlegarAlMinimoDeJava() {
        cuestionarios.add(d.completo(Datos.candidato("FERNÁNDEZ", "LUCAS", TipoDocumento.DNI, "32456789"),
                LocalDateTime.of(2026, 11, 18, 20, 30), 42, 1, "3.63", "4.67", "7.00"));
        cuestionarios.add(d.completo(Datos.candidato("SÁNCHEZ", "PABLO", TipoDocumento.PP, "AB123456"),
                LocalDateTime.of(2026, 11, 14, 10, 21), 44, 2, "6.88", "8.08", "8.75"));
        cuestionarios.add(d.completo(Datos.candidato("GÓMEZ", "MARÍA", TipoDocumento.DNI, "30123456"),
                LocalDateTime.of(2026, 11, 20, 18, 2), 38, 1, "8.50", "7.75", "8.50"));
        Cuestionario rodriguez = d.cuestionario(Datos.candidato("RODRÍGUEZ", "SOFÍA", TipoDocumento.DNI, "35987654"), EstadoCuestionario.INCOMPLETO);
        rodriguez.setFechaInicio(LocalDateTime.of(2026, 11, 15, 19, 10));
        rodriguez.setUltimoIngreso(LocalDateTime.of(2026, 11, 15, 21, 40));
        rodriguez.setCantidadAccesos(3);
        cuestionarios.add(rodriguez);
        cuestionarios.add(d.cuestionario(Datos.candidato("LÓPEZ", "CAMILA", TipoDocumento.DNI, "40111222"), EstadoCuestionario.ACTIVO));
        cuestionarios.add(d.cuestionario(Datos.candidato("DÍAZ", "JULIETA", TipoDocumento.DNI, "38222333"), EstadoCuestionario.ACTIVO));
        cuestionarios.add(d.cuestionario(Datos.candidato("MARTÍNEZ", "DIEGO", TipoDocumento.DNI, "28765432"), EstadoCuestionario.EN_PROCESO));

        OrdenMeritoDTO om = emitir();

        // 1. alcanzaron el minimo en todas: por puntaje
        assertEquals(List.of("GÓMEZ", "SÁNCHEZ"), apellidos(om.getOrdenMerito()));
        assertEquals(new BigDecimal("8.25"), om.getOrdenMerito().get(0).getPuntaje());
        assertEquals(new BigDecimal("7.90"), om.getOrdenMerito().get(1).getPuntaje());
        assertEquals(List.of(1, 2), om.getOrdenMerito().stream().map(FilaOrdenMeritoDTO::getPosicion).toList());
        // 2. FERNANDEZ: JAVA 3,63 < 6 (LEALTAD 7,00 = 7 si alcanza, pero con una que no llegue alcanza para quedar afuera)
        assertEquals(List.of("FERNÁNDEZ"), apellidos(om.getNoAlcanzaronMinimo()));
        assertEquals(new BigDecimal("5.10"), om.getNoAlcanzaronMinimo().getFirst().getPuntaje());
        assertNull(om.getNoAlcanzaronMinimo().getFirst().getPosicion());
        // 3. el resto por estado, en el orden En proceso, Activo, Incompleto y sin puntaje
        assertEquals(List.of(EstadoCuestionario.EN_PROCESO, EstadoCuestionario.ACTIVO, EstadoCuestionario.INCOMPLETO),
                new ArrayList<>(om.getRestoPorEstado().keySet()));
        assertEquals(List.of("DÍAZ", "LÓPEZ"), apellidos(om.getRestoPorEstado().get(EstadoCuestionario.ACTIVO)));
        FilaOrdenMeritoDTO rod = om.getRestoPorEstado().get(EstadoCuestionario.INCOMPLETO).getFirst();
        assertNull(rod.getPuntaje());
        assertEquals(3, rod.getCantidadAccesos());
        assertEquals(LocalDateTime.of(2026, 11, 15, 21, 40), rod.getUltimoIngreso());
        // encabezado
        assertEquals("DEV-JR-01", om.getCodigoPuesto());
        assertEquals("TECNOSUR S.A.", om.getEmpresa());
        assertEquals(AHORA, om.getFechaEmision());

        InOrder orden = inOrder(puestoDAO, cuestionarioDAO);
        orden.verify(puestoDAO).buscarPorId(1L);
        orden.verify(cuestionarioDAO).buscarPorEvaluaciones(List.of(1L, 2L));
    }

    @Test
    void conIgualPuntajeGanaElQueTardoMenosDespuesMenosAccesosYDespuesApellido() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 20, 10, 0);
        cuestionarios.add(d.completo(Datos.candidato("LENTO", "A", TipoDocumento.DNI, "1"), t, 60, 1, "8", "8", "8"));
        cuestionarios.add(d.completo(Datos.candidato("ZAPATA", "A", TipoDocumento.DNI, "2"), t, 30, 2, "8", "8", "8"));
        cuestionarios.add(d.completo(Datos.candidato("ROMERO", "A", TipoDocumento.DNI, "3"), t, 30, 1, "8", "8", "8"));
        cuestionarios.add(d.completo(Datos.candidato("ACOSTA", "B", TipoDocumento.DNI, "4"), t, 30, 1, "8", "8", "8"));
        cuestionarios.add(d.completo(Datos.candidato("ACOSTA", "A", TipoDocumento.DNI, "5"), t, 30, 1, "8", "8", "8"));
        cuestionarios.add(d.completo(Datos.candidato("PRIMERO", "A", TipoDocumento.DNI, "6"), t, 90, 5, "9", "9", "9"));

        OrdenMeritoDTO om = emitir();

        assertEquals(List.of("PRIMERO", "ACOSTA", "ACOSTA", "ROMERO", "ZAPATA", "LENTO"), apellidos(om.getOrdenMerito()));
        assertEquals("A", om.getOrdenMerito().get(1).getNombre());
    }

    @Test
    void losQueNoAlcanzanSeOrdenanPorPuntajeDescendente() {
        LocalDateTime t = LocalDateTime.of(2026, 11, 20, 10, 0);
        cuestionarios.add(d.completo(Datos.candidato("BAJO", "A", TipoDocumento.DNI, "1"), t, 10, 1, "1", "4", "7"));
        cuestionarios.add(d.completo(Datos.candidato("ALTO", "A", TipoDocumento.DNI, "2"), t, 99, 9, "5.99", "10", "10"));

        OrdenMeritoDTO om = emitir();

        assertTrue(om.getOrdenMerito().isEmpty());
        assertEquals(List.of("ALTO", "BAJO"), apellidos(om.getNoAlcanzaronMinimo()));
        assertTrue(om.getRestoPorEstado().isEmpty());
    }

    @Test
    void ignoraCuestionariosDeEvaluacionesDeOtroPuestoYSinResultados() {
        Datos otro = new Datos();
        otro.puesto.setId(2L);
        cuestionarios.add(otro.cuestionario(Datos.candidato("AJENO", "A", TipoDocumento.DNI, "1"), EstadoCuestionario.ACTIVO));
        Cuestionario vacio = d.cuestionario(Datos.candidato("VACIO", "A", TipoDocumento.DNI, "2"), EstadoCuestionario.COMPLETO);
        cuestionarios.add(vacio);

        OrdenMeritoDTO om = emitir();

        assertTrue(om.getRestoPorEstado().isEmpty());
        // sin resultados el puntaje es 0 y alcanza "todas" (no hay ninguna que no llegue)
        assertEquals(new BigDecimal("0.00"), om.getOrdenMerito().getFirst().getPuntaje());
    }

    @Test
    void sinEvaluacionesElegidasOPuestoInexistente() {
        ValidacionException e = assertThrows(ValidacionException.class, () -> reporte.generar(new CriterioReporteDTO(1L, List.of(), null)));
        assertEquals("Debe seleccionar al menos una evaluación", e.getMessage());
        assertThrows(ValidacionException.class, () -> reporte.generar(null));
        assertThrows(ValidacionException.class, () -> reporte.generar(new CriterioReporteDTO(1L, null, null)));
        assertThrows(NoSuchElementException.class, () -> reporte.generar(new CriterioReporteDTO(9L, List.of(1L), null)));
        verifyNoInteractions(cuestionarioDAO);
    }
}
