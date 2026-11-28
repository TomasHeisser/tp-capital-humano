package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.configuracion.RepositorioConfiguracion;
import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dao.RegistroEjecucionDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.BloqueDTO;
import ar.utn.frsf.capitalhumano.dto.CuestionarioDTO;
import ar.utn.frsf.capitalhumano.dto.PreguntaCuestionarioDTO;
import ar.utn.frsf.capitalhumano.dto.RespuestaDTO;
import ar.utn.frsf.capitalhumano.excepcion.RespuestasIncompletasException;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorCuestionarioTest {

    private static final LocalDateTime CREACION = LocalDateTime.of(2026, 11, 20, 10, 0);

    @Mock
    private CuestionarioDAO cuestionarioDAO;
    @Mock
    private RegistroEjecucionDAO registroEjecucionDAO;
    @Mock
    private RepositorioConfiguracion configuracion;

    private LocalDateTime ahora;
    private GestorCuestionario gestor;
    private Cuestionario cuestionario;
    private final AtomicLong ids = new AtomicLong(100);

    @BeforeEach
    void setUp() {
        ahora = CREACION.plusDays(1);
        gestor = new GestorCuestionario(cuestionarioDAO, registroEjecucionDAO, configuracion, () -> ahora, new Random(7));
        // evaluacion con 2 competencias: JAVA (2 factores) y GP (1 factor), 3 preguntas V/F por factor
        Puesto puesto = new Puesto("DEV-JR-01", "DESARROLLADOR JAVA JUNIOR", null, new Empresa("TECNOSUR S.A."));
        Evaluacion ev = new Evaluacion(CREACION, puesto, new Consultor("JPEREZ"));
        ev.agregarCompetenciaEvaluada(competencia("COMP-JAVA", 2));
        ev.agregarCompetenciaEvaluada(competencia("COMP-GP", 1));
        // 3 factores x 2 preguntas = 6 preguntas, bloques de 4 -> 2 bloques (4 y 2)
        cuestionario = new Cuestionario(new Candidato(), "ABCD1234", 4, 120, 7, 5, CREACION);
        cuestionario.setId(1L);
        ev.agregarCuestionario(cuestionario);
    }

    // competencia evaluada con n factores, cada uno con 3 preguntas (VERDADERO vale 10, FALSO 0)
    private CompetenciaEvaluada competencia(String codigo, int factores) {
        CompetenciaEvaluada ce = new CompetenciaEvaluada(new Competencia(codigo, codigo, null), new BigDecimal("5"));
        ce.setId(ids.incrementAndGet());
        for (int f = 0; f < factores; f++) {
            FactorEvaluado fe = new FactorEvaluado(new Factor(codigo + "-F" + f, "F" + f, null, f + 1, null));
            fe.setId(ids.incrementAndGet());
            for (int p = 0; p < 3; p++) {
                PreguntaEvaluada pe = new PreguntaEvaluada();
                pe.setId(ids.incrementAndGet());
                pe.setEnunciado("¿PREGUNTA " + pe.getId() + "?");
                pe.getOpciones().add(opcion(pe, 1, "VERDADERO", "10"));
                pe.getOpciones().add(opcion(pe, 2, "FALSO", "0"));
                fe.agregarPregunta(pe);
            }
            ce.agregarFactor(fe);
        }
        return ce;
    }

    private OpcionEvaluada opcion(PreguntaEvaluada pe, int orden, String desc, String pond) {
        OpcionEvaluada o = new OpcionEvaluada(pe, orden, desc, new BigDecimal(pond));
        o.setId(ids.incrementAndGet());
        return o;
    }

    private void existe() {
        when(cuestionarioDAO.buscarPorId(1L)).thenReturn(Optional.of(cuestionario));
    }

    private List<RespuestaDTO> responder(BloqueDTO b, Function<PreguntaCuestionarioDTO, Integer> indiceOpcion) {
        return b.getPreguntas().stream()
                .map(p -> new RespuestaDTO(p.getIdPregunta(), p.getOpciones().get(indiceOpcion.apply(p)).getId()))
                .toList();
    }

    // ------------------------------------------------------------------ ingresar

    @Test
    void primerIngresoDevuelveInstruccionesYCuentaElAcceso() {
        existe();
        when(configuracion.getInstrucciones()).thenReturn("Lea con atención.");

        CuestionarioDTO dto = gestor.ingresar(1L);

        assertEquals("ACTIVO", dto.getEstado());
        assertTrue(dto.getInstrucciones().startsWith("Lea con atención."));
        assertTrue(dto.getInstrucciones().contains("6 preguntas organizadas en 2 bloques"));
        assertTrue(dto.getInstrucciones().contains("120 minutos") && dto.getInstrucciones().contains("hasta 5 veces"));
        assertEquals("DESARROLLADOR JAVA JUNIOR – TECNOSUR S.A.", dto.getNombrePuesto());
        assertNull(dto.getMensaje());
        assertNull(dto.getBloqueActual());
        assertEquals(1, cuestionario.getCantidadAccesos());
        verify(cuestionarioDAO).actualizar(cuestionario);
    }

    @Test
    void activoConVigenciaVencidaQuedaSinContestar() {
        existe();
        ahora = CREACION.plusDays(8);
        CuestionarioDTO dto = gestor.ingresar(1L);
        assertEquals("SIN_CONTESTAR", dto.getEstado());
        assertEquals(GestorCuestionario.M33, dto.getMensaje());
        verify(cuestionarioDAO).actualizar(cuestionario);
    }

    @Test
    void superarLosAccesosLoDejaIncompleto() {
        existe();
        cuestionario.setCantidadAccesos(5);
        CuestionarioDTO dto = gestor.ingresar(1L);
        assertEquals("INCOMPLETO", dto.getEstado());
        assertEquals(GestorCuestionario.M31, dto.getMensaje());
    }

    @Test
    void enProcesoFueraDeTiempoQuedaIncompleto() {
        existe();
        cuestionario.iniciar(List.of(new BloqueCuestionario(1, List.of())), CREACION);
        ahora = CREACION.plusMinutes(121);
        CuestionarioDTO dto = gestor.ingresar(1L);
        assertEquals("INCOMPLETO", dto.getEstado());
        assertEquals(GestorCuestionario.M32, dto.getMensaje());
    }

    @Test
    void cuestionarioYaCompletoNoSePuedeRetomar() {
        existe();
        cuestionario.cambiarEstado(EstadoCuestionario.COMPLETO, CREACION);
        CuestionarioDTO dto = gestor.ingresar(1L);
        assertEquals("COMPLETO", dto.getEstado());
        assertEquals(GestorCuestionario.NO_DISPONIBLE, dto.getMensaje());
    }

    @Test
    void cuestionarioInexistente() {
        when(cuestionarioDAO.buscarPorId(9L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> gestor.ingresar(9L));
    }

    // ------------------------------------------------------------------ comenzar

    @Test
    void comenzarSorteaDosPorFactorArmaLosBloquesYPasaAEnProceso() {
        existe();
        BloqueDTO b = gestor.comenzar(1L);

        assertEquals(EstadoCuestionario.EN_PROCESO, cuestionario.getEstado().getNombre());
        assertEquals(ahora, cuestionario.getFechaInicio());
        assertEquals(2, cuestionario.getBloques().size());
        assertEquals(4, cuestionario.getBloques().get(0).getPreguntas().size());
        assertEquals(2, cuestionario.getBloques().get(1).getPreguntas().size());
        // de cada factor salen exactamente 2 preguntas
        cuestionario.getEvaluacion().getCompetenciasEvaluadas().forEach(ce -> ce.getFactores().forEach(fe -> {
            long elegidas = cuestionario.getBloques().stream().flatMap(x -> x.getPreguntas().stream())
                    .filter(fe.getPreguntas()::contains).count();
            assertEquals(2, elegidas);
        }));
        assertEquals(1, b.getNroBloque());
        assertEquals(2, b.getTotalBloques());
        assertFalse(b.isEsUltimo());
        assertEquals(2, b.getPreguntas().get(0).getOpciones().size());
        verify(cuestionarioDAO).actualizar(cuestionario);
    }

    @Test
    void noSePuedeComenzarDosVeces() {
        existe();
        gestor.comenzar(1L);
        assertThrows(IllegalStateException.class, () -> gestor.comenzar(1L));
    }

    // ------------------------------------------------------------------ responderBloque

    @Test
    void responderUnBloqueIntermedioDevuelveElSiguiente() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);

        CuestionarioDTO dto = gestor.responderBloque(1L, 1, responder(b1, p -> 0));

        assertEquals("EN_PROCESO", dto.getEstado());
        assertEquals(2, dto.getBloqueActual().getNroBloque());
        assertTrue(dto.getBloqueActual().isEsUltimo());
        assertTrue(cuestionario.getBloques().get(0).estaRespondido());
        assertEquals(4, cuestionario.getBloques().get(0).getRespuestas().size());
    }

    @Test
    void responderElUltimoCalculaLosResultadosYLoCompleta() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);
        CuestionarioDTO medio = gestor.responderBloque(1L, 1, responder(b1, p -> 0));   // todas VERDADERO (10)
        ahora = ahora.plusMinutes(30);
        CuestionarioDTO fin = gestor.responderBloque(1L, 2, responder(medio.getBloqueActual(), p -> 0));

        assertEquals("COMPLETO", fin.getEstado());
        assertEquals(GestorCuestionario.M35, fin.getMensaje());
        assertEquals(ahora, cuestionario.getFechaFin());
        // todas en VERDADERO: cada factor (10 + 10) / 2 = 10 y cada competencia 10
        assertEquals(2, cuestionario.getResultados().size());
        assertEquals(3, cuestionario.getResultadosFactor().size());
        cuestionario.getResultados().forEach(r -> assertEquals(new BigDecimal("10.00"), r.getPuntaje()));
    }

    @Test
    void elPuntajeEsLaSumaSobreDosYElPromedioDeFactores() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);
        // en JAVA el primer factor todo VERDADERO y el segundo todo FALSO; GP una y una
        FactorEvaluado java1 = cuestionario.getEvaluacion().getCompetenciasEvaluadas().get(0).getFactores().get(0);
        FactorEvaluado gp = cuestionario.getEvaluacion().getCompetenciasEvaluadas().get(1).getFactores().get(0);
        Set<Long> gpVerdadero = new HashSet<>();
        Function<PreguntaCuestionarioDTO, Integer> eleccion = p -> {
            if (java1.getPreguntas().stream().anyMatch(x -> x.getId().equals(p.getIdPregunta()))) return 0;
            if (gp.getPreguntas().stream().anyMatch(x -> x.getId().equals(p.getIdPregunta()))) {
                return gpVerdadero.isEmpty() && gpVerdadero.add(p.getIdPregunta()) ? 0 : 1;
            }
            return 1;
        };
        CuestionarioDTO medio = gestor.responderBloque(1L, 1, responder(b1, eleccion));
        gestor.responderBloque(1L, 2, responder(medio.getBloqueActual(), eleccion));

        Map<String, BigDecimal> porCompetencia = new HashMap<>();
        cuestionario.getResultados().forEach(r -> porCompetencia.put(r.getCompetenciaEvaluada().getCodigo(), r.getPuntaje()));
        assertEquals(new BigDecimal("5.00"), porCompetencia.get("COMP-JAVA"));   // (10 + 0) / 2
        assertEquals(new BigDecimal("5.00"), porCompetencia.get("COMP-GP"));     // (10 + 0) / 2
    }

    @Test
    void siFaltanRespuestasInformaCualesConSuNumeroEnElCuestionario() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);
        CuestionarioDTO medio = gestor.responderBloque(1L, 1, responder(b1, p -> 0));
        BloqueDTO b2 = medio.getBloqueActual();
        List<RespuestaDTO> soloLaPrimera = List.of(new RespuestaDTO(b2.getPreguntas().get(0).getIdPregunta(),
                b2.getPreguntas().get(0).getOpciones().get(0).getId()));

        var e = assertThrows(RespuestasIncompletasException.class, () -> gestor.responderBloque(1L, 2, soloLaPrimera));

        assertEquals(GestorCuestionario.M34, e.getMessage());
        assertEquals(List.of("Pregunta 6."), e.getErrores());   // bloque 2: preguntas 5 y 6
        assertFalse(cuestionario.getBloques().get(1).estaRespondido());
    }

    @Test
    void sinRespuestasFaltanTodasYUnaRespuestaSinOpcionNoCuenta() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);
        var e = assertThrows(RespuestasIncompletasException.class, () -> gestor.responderBloque(1L, 1, null));
        assertEquals(4, e.getErrores().size());
        List<RespuestaDTO> sinOpcion = new ArrayList<>(responder(b1, p -> 0));
        sinOpcion.set(0, new RespuestaDTO(sinOpcion.get(0).getIdPregunta(), null));
        var e2 = assertThrows(RespuestasIncompletasException.class, () -> gestor.responderBloque(1L, 1, sinOpcion));
        assertEquals(List.of("Pregunta 1."), e2.getErrores());
    }

    @Test
    void noSePuedeResponderOtroBloqueQueElPendiente() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);
        assertThrows(ValidacionException.class, () -> gestor.responderBloque(1L, 2, responder(b1, p -> 0)));
        gestor.responderBloque(1L, 1, responder(b1, p -> 0));
        // el bloque 1 ya esta respondido: no se puede volver
        assertThrows(ValidacionException.class, () -> gestor.responderBloque(1L, 1, responder(b1, p -> 0)));
    }

    @Test
    void unaPreguntaQueNoEsDelBloqueEsError() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);
        List<RespuestaDTO> respuestas = new ArrayList<>(responder(b1, p -> 0));
        respuestas.add(new RespuestaDTO(9999L, 1L));
        assertThrows(ValidacionException.class, () -> gestor.responderBloque(1L, 1, respuestas));
    }

    @Test
    void siNoEstaEnProcesoNoAceptaRespuestas() {
        existe();
        assertThrows(IllegalStateException.class, () -> gestor.responderBloque(1L, 1, List.of()));
        verify(cuestionarioDAO, never()).actualizar(any());
    }

    @Test
    void reingresoVuelveAlBloquePendiente() {
        existe();
        BloqueDTO b1 = gestor.comenzar(1L);
        gestor.responderBloque(1L, 1, responder(b1, p -> 0));
        ahora = ahora.plusMinutes(10);

        CuestionarioDTO dto = gestor.ingresar(1L);

        assertEquals("EN_PROCESO", dto.getEstado());
        assertEquals(2, dto.getBloqueActual().getNroBloque());
        assertEquals("Bienvenida nuevamente. Se continúa en el bloque 2, que aún no fue respondido.", dto.getMensaje());
        assertNull(dto.getInstrucciones());
    }

    // ------------------------------------------------------------------ CU032

    @Test
    void finalizarVencidosCambiaSoloLosVencidosYDejaRegistro() {
        Cuestionario vencido = new Cuestionario(new Candidato(), "A", 5, 120, 7, 5, CREACION.minusDays(10));
        Cuestionario vigente = new Cuestionario(new Candidato(), "B", 5, 120, 7, 5, CREACION);
        Cuestionario fueraDeTiempo = new Cuestionario(new Candidato(), "C", 5, 120, 7, 5, CREACION);
        fueraDeTiempo.iniciar(List.of(), CREACION);
        when(cuestionarioDAO.buscarPendientes()).thenReturn(List.of(vencido, vigente, fueraDeTiempo));

        int cambiados = gestor.finalizarVencidos();

        assertEquals(2, cambiados);
        assertEquals(EstadoCuestionario.SIN_CONTESTAR, vencido.getEstado().getNombre());
        assertEquals(EstadoCuestionario.ACTIVO, vigente.getEstado().getNombre());
        assertEquals(EstadoCuestionario.INCOMPLETO, fueraDeTiempo.getEstado().getNombre());
        verify(cuestionarioDAO).actualizar(vencido);
        verify(cuestionarioDAO).actualizar(fueraDeTiempo);
        verify(cuestionarioDAO, never()).actualizar(vigente);
        ArgumentCaptor<RegistroEjecucion> registros = ArgumentCaptor.forClass(RegistroEjecucion.class);
        verify(registroEjecucionDAO, times(2)).guardar(registros.capture());
        assertEquals(List.of(AccionEjecucion.INICIO, AccionEjecucion.FIN),
                registros.getAllValues().stream().map(RegistroEjecucion::getAccion).toList());
    }

    @Test
    void elConstructorDeSpringUsaLaHoraReal() {
        GestorCuestionario real = new GestorCuestionario(cuestionarioDAO, registroEjecucionDAO, configuracion);
        when(cuestionarioDAO.buscarPendientes()).thenReturn(List.of());
        assertEquals(0, real.finalizarVencidos());
    }
}
