package ar.utn.frsf.capitalhumano.dominio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transiciones del patron State del cuestionario.
 */
class CuestionarioTest {

    private static final LocalDateTime CREACION = LocalDateTime.of(2026, 11, 20, 10, 0);

    private Cuestionario c;

    @BeforeEach
    void setUp() {
        // 5 por bloque, 120 minutos, 7 dias de vigencia, 5 accesos
        c = new Cuestionario(new Candidato(), "ABCD1234", 5, 120, 7, 5, CREACION);
        c.setId(1L);
    }

    @Test
    void arrancaActivoSinAccesos() {
        assertEquals(EstadoCuestionario.ACTIVO, c.getEstado().getNombre());
        assertEquals(0, c.getCantidadAccesos());
        assertEquals(CREACION, c.getFechaCambioEstado());
    }

    @Test
    void primerIngresoDentroDeLaVigenciaPuedeSeguirYSumaAcceso() {
        LocalDateTime ahora = CREACION.plusDays(1);
        assertTrue(c.registrarAcceso(ahora));
        assertEquals(1, c.getCantidadAccesos());
        assertEquals(ahora, c.getUltimoIngreso());
        assertEquals(EstadoCuestionario.ACTIVO, c.getEstado().getNombre());
    }

    @Test
    void activoConVigenciaVencidaPasaASinContestar() {
        LocalDateTime ahora = CREACION.plusDays(8);
        assertFalse(c.registrarAcceso(ahora));
        assertEquals(EstadoCuestionario.SIN_CONTESTAR, c.getEstado().getNombre());
        assertEquals(ahora, c.getFechaCambioEstado());
    }

    @Test
    void activoQueSuperaLosAccesosPasaAIncompleto() {
        c.setCantidadAccesos(5);
        assertFalse(c.registrarAcceso(CREACION.plusHours(1)));
        assertEquals(EstadoCuestionario.INCOMPLETO, c.getEstado().getNombre());
        assertTrue(c.superoMaxAccesos());
    }

    @Test
    void iniciarPasaAEnProcesoGuardaFechaInicioYBloques() {
        LocalDateTime ahora = CREACION.plusHours(2);
        BloqueCuestionario b = new BloqueCuestionario(1, List.of());
        c.iniciar(List.of(b), ahora);
        assertEquals(EstadoCuestionario.EN_PROCESO, c.getEstado().getNombre());
        assertEquals(ahora, c.getFechaInicio());
        assertEquals(1, c.getBloques().size());
        assertEquals(1L, b.getIdCuestionario());
        assertSame(c, b.getCuestionario());
    }

    @Test
    void enProcesoDentroDelTiempoPuedeSeguir() {
        c.iniciar(List.of(), CREACION);
        assertTrue(c.registrarAcceso(CREACION.plusMinutes(120)));
        assertEquals(EstadoCuestionario.EN_PROCESO, c.getEstado().getNombre());
    }

    @Test
    void enProcesoFueraDeTiempoPasaAIncompleto() {
        c.iniciar(List.of(), CREACION);
        assertFalse(c.registrarAcceso(CREACION.plusMinutes(121)));
        assertEquals(EstadoCuestionario.INCOMPLETO, c.getEstado().getNombre());
    }

    @Test
    void enProcesoQueSuperaLosAccesosPasaAIncompleto() {
        c.iniciar(List.of(), CREACION);
        c.setCantidadAccesos(5);
        assertFalse(c.registrarAcceso(CREACION.plusMinutes(10)));
        assertEquals(EstadoCuestionario.INCOMPLETO, c.getEstado().getNombre());
    }

    @Test
    void completarGuardaResultadosYFechaFin() {
        c.iniciar(List.of(), CREACION);
        CompetenciaEvaluada ce = new CompetenciaEvaluada();
        ce.setId(7L);
        FactorEvaluado fe = new FactorEvaluado();
        fe.setId(9L);
        Resultado r = new Resultado(ce, new java.math.BigDecimal("8.50"), List.of(new ResultadoFactor(fe, new java.math.BigDecimal("8.50"))));
        LocalDateTime fin = CREACION.plusMinutes(40);

        c.completar(List.of(r), fin);

        assertEquals(EstadoCuestionario.COMPLETO, c.getEstado().getNombre());
        assertEquals(fin, c.getFechaFin());
        assertEquals(1, c.getResultados().size());
        assertEquals(1, c.getResultadosFactor().size());
        assertEquals(1L, r.getIdCuestionario());
        assertEquals(1L, c.getResultadosFactor().get(0).getIdCuestionario());
    }

    @Test
    void noSePuedeCompletarUnoActivoNiIniciarUnoEnProceso() {
        assertThrows(IllegalStateException.class, () -> c.completar(List.of(), CREACION));
        c.iniciar(List.of(), CREACION);
        assertThrows(IllegalStateException.class, () -> c.iniciar(List.of(), CREACION));
    }

    @Test
    void losEstadosFinalesNoDejanIngresarNiVencen() {
        for (String estado : List.of(EstadoCuestionario.COMPLETO, EstadoCuestionario.INCOMPLETO, EstadoCuestionario.SIN_CONTESTAR)) {
            c.cambiarEstado(estado, CREACION);
            assertFalse(c.registrarAcceso(CREACION.plusDays(30)));
            assertFalse(c.verificarVencimiento(CREACION.plusDays(30)));
            assertEquals(estado, c.getEstado().getNombre());
            assertEquals(estado, c.getEstado().toString());
        }
    }

    @Test
    void verificarVencimientoDeActivoYEnProceso() {
        assertFalse(c.verificarVencimiento(CREACION.plusDays(7)));
        assertTrue(c.verificarVencimiento(CREACION.plusDays(7).plusMinutes(1)));
        assertEquals(EstadoCuestionario.SIN_CONTESTAR, c.getEstado().getNombre());

        Cuestionario otro = new Cuestionario(new Candidato(), "X", 5, 120, 7, 5, CREACION);
        otro.iniciar(List.of(), CREACION);
        assertFalse(otro.verificarVencimiento(CREACION.plusMinutes(60)));
        assertTrue(otro.verificarVencimiento(CREACION.plusMinutes(130)));
        assertEquals(EstadoCuestionario.INCOMPLETO, otro.getEstado().getNombre());
    }

    @Test
    void bloquePendienteEsElPrimeroSinResponder() {
        BloqueCuestionario b1 = new BloqueCuestionario(1, List.of());
        BloqueCuestionario b2 = new BloqueCuestionario(2, List.of());
        c.iniciar(List.of(b1, b2), CREACION);
        b1.setRespondido(true);
        assertSame(b2, c.getBloquePendiente());
        b2.setRespondido(true);
        assertNull(c.getBloquePendiente());
    }
}
