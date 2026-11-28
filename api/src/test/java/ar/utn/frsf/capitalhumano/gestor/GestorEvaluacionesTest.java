package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.configuracion.RepositorioConfiguracion;
import ar.utn.frsf.capitalhumano.dao.*;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.excepcion.CompetenciasNoEvaluablesException;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import ar.utn.frsf.capitalhumano.exportacion.ExportadorClaves;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorEvaluacionesTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 27, 12, 0);

    @Mock
    private CandidatoDAO candidatoDAO;
    @Mock
    private CuestionarioDAO cuestionarioDAO;
    @Mock
    private PuestoDAO puestoDAO;
    @Mock
    private EvaluacionDAO evaluacionDAO;
    @Mock
    private ConsultorDAO consultorDAO;
    @Mock
    private RepositorioConfiguracion configuracion;
    @Mock
    private ExportadorClaves exportador;

    private GestorEvaluaciones gestor;
    private Puesto puesto;
    private Competencia java;
    private Competencia comunicacion;
    private Candidato acosta;
    private Candidato pereyra;
    private final AtomicInteger sorteo = new AtomicInteger();

    @BeforeEach
    void setUp() {
        // las claves salen ABCDEFGH, IJKLMNOP, ... para poder comparar
        gestor = new GestorEvaluaciones(candidatoDAO, cuestionarioDAO, puestoDAO, evaluacionDAO, consultorDAO, configuracion,
                exportador, () -> AHORA, n -> sorteo.getAndIncrement() % n);
        OpcionRespuesta vf = new OpcionRespuesta("VERDADERO / FALSO", null);
        vf.agregarItem(1, "VERDADERO");
        vf.agregarItem(2, "FALSO");
        vf.getItems().get(0).setId(11L);
        vf.getItems().get(1).setId(12L);
        java = new Competencia("COMP-JAVA", "PROGRAMACIÓN JAVA", null);
        java.setId(3L);
        factor(java, "FAC-JAVA-POO", 1, vf, 3, false);
        factor(java, "FAC-JAVA-COL", 2, vf, 1, false);   // una sola pregunta activa: no se copia
        factor(java, "FAC-JAVA-BAJA", 3, vf, 3, true);   // dado de baja: no se copia
        comunicacion = new Competencia("COMP-COM", "COMUNICACIÓN", null);
        comunicacion.setId(4L);
        factor(comunicacion, "FAC-COM-ESC", 1, vf, 1, false);
        puesto = new Puesto("DEV-JR-01", "DESARROLLADOR JAVA JUNIOR", null, new Empresa("TECNOSUR S.A."));
        puesto.setId(1L);
        puesto.agregarCompetencia(java, new BigDecimal("6"));
        acosta = candidato(8L, 1008L, "ACOSTA", "MARTÍN", "33444555");
        pereyra = candidato(9L, 1009L, "PEREYRA", "LUCÍA", "36777888");
    }

    private static void factor(Competencia c, String codigo, int orden, OpcionRespuesta op, int preguntas, boolean baja) {
        Factor f = new Factor(codigo, codigo, null, orden, c);
        if (baja) f.setFechaBaja(LocalDateTime.of(2026, 1, 1, 0, 0));
        for (int i = 0; i < preguntas; i++) {
            Pregunta p = new Pregunta(codigo + "-P" + i, "¿" + codigo + " " + i + "?", null, f, op);
            p.agregarPonderacion(op.getItems().get(0), BigDecimal.TEN);
            p.agregarPonderacion(op.getItems().get(1), BigDecimal.ZERO);
            f.getPreguntas().add(p);
        }
        // una dada de baja mas, que no tiene que contar
        Pregunta vieja = new Pregunta(codigo + "-VIEJA", "?", null, f, op);
        vieja.darDeBaja(LocalDateTime.of(2026, 1, 1, 0, 0));
        f.getPreguntas().add(vieja);
        c.getFactores().add(f);
    }

    private static Candidato candidato(Long id, Long nro, String apellido, String nombre, String doc) {
        Candidato c = new Candidato();
        c.setId(id);
        c.setNroCandidato(nro);
        c.setApellido(apellido);
        c.setNombre(nombre);
        c.setTipoDocumento(TipoDocumento.DNI);
        c.setNroDocumento(doc);
        return c;
    }

    // ------------------------------------------------------------------ pasos 3 a 6

    @Test
    void buscarCandidatosPideAlMenosUnFiltro() {
        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.buscarCandidatos(new FiltroCandidatoDTO(" ", "", null)));
        assertEquals("Debe completar al menos un filtro", e.getMessage());
        assertThrows(ValidacionException.class, () -> gestor.buscarCandidatos(null));
        verifyNoInteractions(candidatoDAO);
    }

    @Test
    void buscarCandidatosArmaLasFilas() {
        when(candidatoDAO.buscarPorFiltros("acos", null, null)).thenReturn(List.of(acosta));
        when(candidatoDAO.buscarPorFiltros(null, null, 1009L)).thenReturn(List.of(pereyra));

        assertEquals(List.of(new CandidatoGestionDTO(8L, 1008L, "ACOSTA", "MARTÍN", TipoDocumento.DNI, "33444555")),
                gestor.buscarCandidatos(new FiltroCandidatoDTO("acos", null, null)));
        assertEquals("PEREYRA", gestor.buscarCandidatos(new FiltroCandidatoDTO(null, null, 1009L)).getFirst().getApellido());
        assertEquals(1, gestor.buscarCandidatos(new FiltroCandidatoDTO(null, "x", null)).size() + 1);
    }

    @Test
    void verificarActivosDevuelveSoloLosQueTienenUnoActivoOEnProceso() {
        Cuestionario enProceso = new Cuestionario(pereyra, "AAAA1111", 5, 120, 7, 5, AHORA);
        when(cuestionarioDAO.buscarActivoOEnProceso(8L)).thenReturn(Optional.empty());
        when(cuestionarioDAO.buscarActivoOEnProceso(9L)).thenReturn(Optional.of(enProceso));

        List<CandidatoGestionDTO> conActivos = gestor.verificarCuestionariosActivos(List.of(8L, 9L));

        assertEquals(List.of("PEREYRA"), conActivos.stream().map(CandidatoGestionDTO::getApellido).toList());
        assertTrue(gestor.verificarCuestionariosActivos(null).isEmpty());
    }

    // ------------------------------------------------------------------ pasos 8 y 9

    @Test
    void validarPuestoDevuelveLasCompetenciasConSuPonderacion() {
        when(puestoDAO.buscarPorId(1L)).thenReturn(Optional.of(puesto));

        assertEquals(List.of(new CompetenciaRequeridaDTO(3L, "PROGRAMACIÓN JAVA", new BigDecimal("6"))), gestor.validarPuesto(1L));
    }

    @Test
    void validarPuestoConUnaCompetenciaSinFactoresEvaluablesEs9A() {
        puesto.agregarCompetencia(comunicacion, new BigDecimal("6"));
        when(puestoDAO.buscarPorId(1L)).thenReturn(Optional.of(puesto));

        CompetenciasNoEvaluablesException e = assertThrows(CompetenciasNoEvaluablesException.class, () -> gestor.validarPuesto(1L));

        assertEquals("Las siguientes competencias del puesto no pueden ser evaluadas:", e.getMessage());
        assertEquals(List.of("COMUNICACIÓN"), e.getErrores());
    }

    @Test
    void validarPuestoInexistenteODadoDeBaja() {
        when(puestoDAO.buscarPorId(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> gestor.validarPuesto(2L));
        puesto.setFechaBaja(AHORA);
        when(puestoDAO.buscarPorId(1L)).thenReturn(Optional.of(puesto));
        assertThrows(NoSuchElementException.class, () -> gestor.validarPuesto(1L));
    }

    // ------------------------------------------------------------------ paso 10

    @Test
    void generarClavesDeOchoLetrasYDigitos() {
        when(candidatoDAO.buscarPorId(8L)).thenReturn(Optional.of(acosta));
        when(candidatoDAO.buscarPorId(9L)).thenReturn(Optional.of(pereyra));

        List<CandidatoClaveDTO> claves = gestor.generarClaves(List.of(8L, 9L));

        assertEquals(List.of("ABCDEFGH", "IJKLMNOP"), claves.stream().map(CandidatoClaveDTO::getClave).toList());
        assertEquals("ACOSTA", claves.getFirst().getApellido());
        assertThrows(ValidacionException.class, () -> gestor.generarClaves(List.of()));
        when(candidatoDAO.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> gestor.generarClaves(List.of(99L)));
    }

    @Test
    void lasClavesUsanTodoElAlfabeto() {
        GestorEvaluaciones real = new GestorEvaluaciones(candidatoDAO, cuestionarioDAO, puestoDAO, evaluacionDAO, consultorDAO,
                configuracion, exportador);
        when(candidatoDAO.buscarPorId(8L)).thenReturn(Optional.of(acosta));
        assertTrue(real.generarClaves(List.of(8L)).getFirst().getClave().matches("[A-Z0-9]{8}"));
    }

    // ------------------------------------------------------------------ paso 12

    @Test
    void registrarCopiaElModeloYCreaUnCuestionarioActivoPorCandidato() {
        Consultor jperez = new Consultor("JPEREZ");
        when(puestoDAO.buscarPorId(1L)).thenReturn(Optional.of(puesto));
        when(consultorDAO.buscarPorUsername("JPEREZ")).thenReturn(Optional.of(jperez));
        when(configuracion.getPreguntasPorBloque()).thenReturn(5);
        when(configuracion.getTiempoMaximoMinutos()).thenReturn(120);
        when(configuracion.getVigenciaDias()).thenReturn(7);
        when(configuracion.getMaxAccesos()).thenReturn(5);
        when(candidatoDAO.buscarPorId(8L)).thenReturn(Optional.of(acosta));
        when(candidatoDAO.buscarPorId(9L)).thenReturn(Optional.of(pereyra));
        when(evaluacionDAO.guardar(any())).thenReturn(true);
        NuevaEvaluacionDTO datos = new NuevaEvaluacionDTO(1L, List.of(
                new CandidatoClaveDTO(8L, "ACOSTA", "MARTÍN", TipoDocumento.DNI, "33444555", "abcd1234"),
                new CandidatoClaveDTO(9L, "PEREYRA", "LUCÍA", TipoDocumento.DNI, "36777888", "WXYZ9876")));

        EvaluacionDTO r = gestor.registrarEvaluacion(datos, "JPEREZ");

        InOrder orden = inOrder(puestoDAO, consultorDAO, configuracion, candidatoDAO, evaluacionDAO);
        orden.verify(puestoDAO).buscarPorId(1L);
        orden.verify(consultorDAO).buscarPorUsername("JPEREZ");
        orden.verify(configuracion).getPreguntasPorBloque();
        orden.verify(candidatoDAO).buscarPorId(8L);
        ArgumentCaptor<Evaluacion> guardada = ArgumentCaptor.forClass(Evaluacion.class);
        orden.verify(evaluacionDAO).guardar(guardada.capture());

        Evaluacion ev = guardada.getValue();
        assertEquals(AHORA, ev.getFechaCreacion());
        assertSame(jperez, ev.getConsultor());
        // mismo modelo: solo el factor con 2 o mas preguntas activas, con sus 3 preguntas y las opciones ponderadas
        CompetenciaEvaluada ce = ev.getCompetenciasEvaluadas().getFirst();
        assertEquals("PROGRAMACIÓN JAVA", ce.getNombre());
        assertEquals(new BigDecimal("6"), ce.getPonderacionMinima());
        assertEquals(List.of("FAC-JAVA-POO"), ce.getFactores().stream().map(FactorEvaluado::getCodigo).toList());
        FactorEvaluado fe = ce.getFactores().getFirst();
        assertEquals(3, fe.getPreguntas().size());
        assertEquals(BigDecimal.TEN, fe.getPreguntas().getFirst().getOpciones().getFirst().getPonderacion());
        // cuestionarios activos con las claves mostradas y los parametros de configuracion
        assertEquals(2, ev.getCuestionarios().size());
        Cuestionario c = ev.getCuestionarios().getFirst();
        assertEquals("ABCD1234", c.getClave());
        assertEquals(EstadoCuestionario.ACTIVO, c.getEstado().getNombre());
        assertEquals(5, c.getPreguntasPorBloque());
        assertEquals(120, c.getTiempoMaximoMinutos());
        assertEquals(AHORA, c.getFechaCreacion());
        assertSame(acosta, c.getCandidato());
        assertEquals(new EvaluacionDTO(null, AHORA, "DESARROLLADOR JAVA JUNIOR", "TECNOSUR S.A.", 2), r);
    }

    @Test
    void registrarValidaLosDatosAntesDeTocarLaBase() {
        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.registrarEvaluacion(new NuevaEvaluacionDTO(null, List.of(
                new CandidatoClaveDTO(8L, null, null, null, null, "CORTA"),
                new CandidatoClaveDTO(8L, null, null, null, null, "ABCD1234"))), "JPEREZ"));
        assertEquals("No se pudo registrar la evaluación. Corrija lo siguiente:", e.getMessage());
        assertEquals(List.of("Se debe seleccionar una función", "Hay candidatos repetidos en la evaluación",
                "Cada clave de ingreso debe tener 8 letras o dígitos"), e.getErrores());
        ValidacionException e2 = assertThrows(ValidacionException.class, () -> gestor.registrarEvaluacion(null, "JPEREZ"));
        assertTrue(e2.getErrores().contains("Se debe seleccionar uno o más candidatos"));
        verifyNoInteractions(puestoDAO, evaluacionDAO);
    }

    @Test
    void registrarConConsultorDesconocido() {
        when(puestoDAO.buscarPorId(1L)).thenReturn(Optional.of(puesto));
        when(consultorDAO.buscarPorUsername("NADIE")).thenReturn(Optional.empty());
        NuevaEvaluacionDTO datos = new NuevaEvaluacionDTO(1L, List.of(new CandidatoClaveDTO(8L, null, null, null, null, "ABCD1234")));
        assertThrows(NoSuchElementException.class, () -> gestor.registrarEvaluacion(datos, "NADIE"));
        verify(evaluacionDAO, never()).guardar(any());
    }

    // ------------------------------------------------------------------ pasos 14 a 17

    @Test
    void exportarClavesMandaPuestoFechaYClavesAlExportador() {
        Evaluacion ev = new Evaluacion(LocalDateTime.of(2026, 11, 26, 10, 30), puesto, new Consultor("JPEREZ"));
        ev.agregarCuestionario(new Cuestionario(acosta, "T6Q9D2LM", 5, 120, 7, 5, ev.getFechaCreacion()));
        ArchivoDTO archivo = new ArchivoDTO("x.xlsx", new byte[]{1});
        when(evaluacionDAO.buscarPorId(2L)).thenReturn(Optional.of(ev));
        when(exportador.exportar("DESARROLLADOR JAVA JUNIOR", ev.getFechaCreacion(),
                List.of(new CandidatoClaveDTO(8L, "ACOSTA", "MARTÍN", TipoDocumento.DNI, "33444555", "T6Q9D2LM")))).thenReturn(archivo);

        assertSame(archivo, gestor.exportarClaves(2L));
        when(evaluacionDAO.buscarPorId(3L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> gestor.exportarClaves(3L));
    }
}
