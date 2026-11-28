package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.utn.frsf.capitalhumano.dao.OpcionRespuestaDAO;
import ar.utn.frsf.capitalhumano.dao.PreguntaDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.excepcion.PreguntaDuplicadaException;
import ar.utn.frsf.capitalhumano.excepcion.ServicioIAException;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import ar.utn.frsf.capitalhumano.integracion.IAsistenteIA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorPreguntasTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 27, 10, 30);

    @Mock
    private PreguntaDAO preguntaDAO;
    @Mock
    private FactorDAO factorDAO;
    @Mock
    private OpcionRespuestaDAO opcionRespuestaDAO;
    @Mock
    private ConsultorDAO consultorDAO;
    @Mock
    private GestorAuditoria gestorAuditoria;
    @Mock
    private IAsistenteIA asistenteIA;

    private GestorPreguntas gestor;
    private Competencia gp;
    private Factor tiempo;
    private OpcionRespuesta vf;
    private OpcionRespuesta escala;

    @BeforeEach
    void setUp() {
        gestor = new GestorPreguntas(preguntaDAO, factorDAO, opcionRespuestaDAO, consultorDAO, gestorAuditoria, asistenteIA, () -> AHORA);
        gp = new Competencia("COMP-GP", "GESTIÓN DE PROYECTOS", null);
        gp.setId(1L);
        tiempo = new Factor("FAC-GP-TIE", "GESTIÓN DEL TIEMPO", null, 2, gp);
        tiempo.setId(2L);
        vf = opcion(10L, "VERDADERO / FALSO", "VERDADERO", "FALSO");
        escala = opcion(20L, "ESCALA DE 5 NIVELES", "MALO", "REGULAR", "BUENO", "MUY BUENO", "EXCELENTE");
    }

    // conjunto con items de id = id del conjunto + orden
    private static OpcionRespuesta opcion(Long id, String nombre, String... items) {
        OpcionRespuesta o = new OpcionRespuesta(nombre, null);
        o.setId(id);
        for (int i = 0; i < items.length; i++) {
            o.agregarItem(i + 1, items[i]);
            o.getItems().get(i).setId(id + i + 1);
        }
        return o;
    }

    private Pregunta pregunta(Long id, String nombre, String enunciado, OpcionRespuesta op, String... valores) {
        Pregunta p = new Pregunta(nombre, enunciado, null, tiempo, op);
        p.setId(id);
        for (int i = 0; i < valores.length; i++) {
            p.agregarPonderacion(op.getItems().get(i), new BigDecimal(valores[i]));
        }
        return p;
    }

    // pregunta V/F valida, con VERDADERO 10 y FALSO 0
    private PreguntaDTO datosVF() {
        List<PonderacionDTO> pond = new ArrayList<>(List.of(
                new PonderacionDTO(11L, 1, "VERDADERO", new BigDecimal("10")),
                new PonderacionDTO(12L, 2, "FALSO", new BigDecimal("0"))));
        return new PreguntaDTO(null, 1L, 2L, " valor ganado ", "¿el valor ganado mide el avance?", "  ", 10L, pond);
    }

    // ------------------------------------------------------------------ CU020

    @Test
    void buscarSinCriteriosTiraValidacionYNoConsulta() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> gestor.buscarPreguntas(new FiltroPreguntaDTO(null, null, "   ")));
        assertEquals("Debe ingresar al menos un criterio de búsqueda", e.getMessage());
        assertThrows(ValidacionException.class, () -> gestor.buscarPreguntas(null));
        verifyNoInteractions(preguntaDAO);
    }

    @Test
    void buscarArmaLasFilasConCompetenciaYFactor() {
        when(preguntaDAO.buscarPorFiltros(1L, null, null))
                .thenReturn(List.of(pregunta(5L, "VALOR GANADO", "¿EL VALOR GANADO...?", vf, "10", "0")));

        List<PreguntaResumenDTO> filas = gestor.buscarPreguntas(new FiltroPreguntaDTO(1L, null, null));

        assertEquals(1, filas.size());
        PreguntaResumenDTO f = filas.getFirst();
        assertEquals(5L, f.getId());
        assertEquals("GESTIÓN DE PROYECTOS", f.getCompetencia());
        assertEquals("GESTIÓN DEL TIEMPO", f.getFactor());
        assertEquals("VALOR GANADO", f.getNombre());
        assertEquals("¿EL VALOR GANADO...?", f.getEnunciado());
    }

    @Test
    void buscarSoloPorFactorOPorNombreAlcanza() {
        when(preguntaDAO.buscarPorFiltros(any(), any(), any())).thenReturn(List.of());
        assertTrue(gestor.buscarPreguntas(new FiltroPreguntaDTO(null, 2L, null)).isEmpty());
        assertTrue(gestor.buscarPreguntas(new FiltroPreguntaDTO(null, null, "gan")).isEmpty());
        verify(preguntaDAO).buscarPorFiltros(null, null, "gan");
    }

    // ------------------------------------------------------------------ CU022 obtener

    @Test
    void obtenerDevuelveLasPonderacionesPorOrden() {
        Pregunta p = pregunta(5L, "VALOR GANADO", "¿...?", vf, "10", "0");
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));

        PreguntaDTO d = gestor.obtenerPregunta(5L);

        assertEquals(1L, d.getIdCompetencia());
        assertEquals(2L, d.getIdFactor());
        assertEquals(10L, d.getIdOpcionRespuesta());
        assertEquals(List.of("VERDADERO", "FALSO"), d.getPonderaciones().stream().map(PonderacionDTO::getDescripcion).toList());
        assertEquals(0, new BigDecimal("10").compareTo(d.getPonderaciones().getFirst().getPonderacion()));
    }

    @Test
    void obtenerUnaDadaDeBajaOInexistenteEs404() {
        Pregunta p = pregunta(5L, "X", "Y", vf, "10", "0");
        p.darDeBaja(AHORA);
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));
        when(preguntaDAO.buscarPorId(6L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> gestor.obtenerPregunta(5L));
        assertThrows(NoSuchElementException.class, () -> gestor.obtenerPregunta(6L));
    }

    // ------------------------------------------------------------------ CU021

    @Test
    void registrarGuardaLaPreguntaNormalizadaConSusPonderaciones() {
        when(preguntaDAO.buscarCoincidencias(2L, "VALOR GANADO", "¿EL VALOR GANADO MIDE EL AVANCE?", null)).thenReturn(List.of());
        when(factorDAO.buscarPorId(2L)).thenReturn(Optional.of(tiempo));
        when(opcionRespuestaDAO.buscarPorId(10L)).thenReturn(Optional.of(vf));
        when(preguntaDAO.guardar(any())).thenReturn(true);

        assertTrue(gestor.registrarPregunta(datosVF()));

        // el orden de los mensajes es el de la secuencia de CU021
        InOrder orden = inOrder(preguntaDAO, factorDAO, opcionRespuestaDAO);
        orden.verify(preguntaDAO).buscarCoincidencias(any(), anyString(), anyString(), any());
        orden.verify(factorDAO).buscarPorId(2L);
        orden.verify(opcionRespuestaDAO).buscarPorId(10L);
        ArgumentCaptor<Pregunta> guardada = ArgumentCaptor.forClass(Pregunta.class);
        orden.verify(preguntaDAO).guardar(guardada.capture());

        Pregunta p = guardada.getValue();
        assertEquals("VALOR GANADO", p.getNombre());
        assertEquals("¿EL VALOR GANADO MIDE EL AVANCE?", p.getEnunciado());
        assertNull(p.getDescripcion());
        assertSame(tiempo, p.getFactor());
        assertSame(vf, p.getOpcionRespuesta());
        assertEquals(2, p.getPonderaciones().size());
        assertEquals(0, BigDecimal.TEN.compareTo(p.getPonderacion(vf.getItems().getFirst())));
    }

    @Test
    void registrarJuntaTodasLasOmisiones() {
        PreguntaDTO d = new PreguntaDTO(null, null, null, " ", null, null, null, null);

        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d));

        assertEquals("No se pudo registrar la pregunta. Corrija lo siguiente:", e.getMessage());
        assertEquals(List.of("Debe seleccionar la Competencia.", "Debe seleccionar el Factor.",
                "Debe ingresar el Nombre de pregunta.", "Debe ingresar la Pregunta.",
                "Debe seleccionar la Opción de respuesta."), e.getErrores());
        verifyNoInteractions(preguntaDAO, factorDAO, opcionRespuestaDAO);
    }

    @Test
    void registrarAvisaLasLongitudesExcedidas() {
        PreguntaDTO d = datosVF();
        d.setNombre("N".repeat(118));
        d.setEnunciado("E".repeat(501));
        d.setDescripcion("D".repeat(600));

        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d));

        assertEquals(List.of("El Nombre de pregunta excede la longitud máxima de 100 caracteres (tiene 118).",
                "La Pregunta excede la longitud máxima de 500 caracteres (tiene 501).",
                "La Descripción excede la longitud máxima de 500 caracteres (tiene 600)."), e.getErrores());
    }

    @Test
    void registrarConPonderacionesQueNoSumanDiez() {
        PreguntaDTO d = datosVF();
        d.getPonderaciones().get(1).setPonderacion(new BigDecimal("1"));

        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d));

        // solo falla la suma: el panel no dice "Corrija lo siguiente"
        assertEquals("No se pudo registrar la pregunta:", e.getMessage());
        assertEquals(List.of("Las ponderaciones deben ser 0 o positivas y sumar 10 (actualmente suman 11)."), e.getErrores());
    }

    @Test
    void registrarConPonderacionNegativaODecimalesDeMas() {
        PreguntaDTO d = datosVF();
        d.getPonderaciones().get(0).setPonderacion(new BigDecimal("10.5"));
        d.getPonderaciones().get(1).setPonderacion(new BigDecimal("-0.5"));
        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d));
        assertEquals(List.of("Las ponderaciones deben ser 0 o positivas y sumar 10 (actualmente suman 10)."), e.getErrores());

        PreguntaDTO d2 = datosVF();
        d2.getPonderaciones().get(0).setPonderacion(new BigDecimal("9.999"));
        d2.getPonderaciones().get(1).setPonderacion(new BigDecimal("0.001"));
        ValidacionException e2 = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d2));
        assertEquals("La Ponderación de la opción VERDADERO admite hasta 2 decimales.", e2.getErrores().getFirst());
        assertEquals("No se pudo registrar la pregunta. Corrija lo siguiente:", e2.getMessage());
    }

    @Test
    void registrarConPonderacionVaciaOSinPonderaciones() {
        PreguntaDTO d = datosVF();
        d.getPonderaciones().get(1).setPonderacion(null);
        d.getPonderaciones().get(1).setDescripcion(null);
        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d));
        // sin descripcion se nombra la opcion por su orden; y la suma no se controla hasta que esten todas
        assertEquals(List.of("Debe ingresar la Ponderación de la opción 2."), e.getErrores());

        PreguntaDTO d2 = datosVF();
        d2.setPonderaciones(null);
        ValidacionException e2 = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d2));
        assertEquals(List.of("Debe ingresar la Ponderación de cada opción."), e2.getErrores());
    }

    @Test
    void registrarDuplicadaPorNombre() {
        when(preguntaDAO.buscarCoincidencias(any(), any(), any(), any()))
                .thenReturn(List.of(pregunta(7L, "VALOR GANADO", "OTRO ENUNCIADO", vf)));

        PreguntaDuplicadaException e = assertThrows(PreguntaDuplicadaException.class, () -> gestor.registrarPregunta(datosVF()));

        assertEquals("No se pudo registrar la pregunta:", e.getMessage());
        assertEquals(List.of("Ya existe una pregunta con el nombre VALOR GANADO para la competencia GESTIÓN DE PROYECTOS"
                + " y el factor GESTIÓN DEL TIEMPO."), e.getErrores());
        verify(preguntaDAO, never()).guardar(any());
    }

    @Test
    void registrarDuplicadaPorEnunciadoOPorAmbos() {
        when(preguntaDAO.buscarCoincidencias(any(), any(), any(), any()))
                .thenReturn(List.of(pregunta(7L, "OTRO", "¿EL VALOR GANADO MIDE EL AVANCE?", vf)))
                .thenReturn(List.of(pregunta(7L, "VALOR GANADO", "¿EL VALOR GANADO MIDE EL AVANCE?", vf)));

        PreguntaDuplicadaException e1 = assertThrows(PreguntaDuplicadaException.class, () -> gestor.registrarPregunta(datosVF()));
        assertTrue(e1.getErrores().getFirst().startsWith("Ya existe una pregunta con ese enunciado para la competencia"));

        PreguntaDuplicadaException e2 = assertThrows(PreguntaDuplicadaException.class, () -> gestor.registrarPregunta(datosVF()));
        assertTrue(e2.getErrores().getFirst().startsWith("Ya existe una pregunta con el nombre VALOR GANADO y con ese enunciado"));
    }

    @Test
    void registrarSinPonderarTodasLasOpcionesDelConjunto() {
        PreguntaDTO d = datosVF();
        d.setIdOpcionRespuesta(20L);
        d.setPonderaciones(new ArrayList<>(List.of(new PonderacionDTO(21L, 1, "MALO", BigDecimal.TEN))));
        when(preguntaDAO.buscarCoincidencias(any(), any(), any(), any())).thenReturn(List.of());
        when(factorDAO.buscarPorId(2L)).thenReturn(Optional.of(tiempo));
        when(opcionRespuestaDAO.buscarPorId(20L)).thenReturn(Optional.of(escala));

        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.registrarPregunta(d));

        assertEquals(List.of("Debe ingresar la Ponderación de cada opción de ESCALA DE 5 NIVELES."), e.getErrores());
        verify(preguntaDAO, never()).guardar(any());
    }

    @Test
    void registrarConFactorUOpcionInexistente() {
        when(preguntaDAO.buscarCoincidencias(any(), any(), any(), any())).thenReturn(List.of());
        when(factorDAO.buscarPorId(2L)).thenReturn(Optional.empty()).thenReturn(Optional.of(tiempo));
        when(opcionRespuestaDAO.buscarPorId(10L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> gestor.registrarPregunta(datosVF()));
        assertThrows(NoSuchElementException.class, () -> gestor.registrarPregunta(datosVF()));
    }

    // ------------------------------------------------------------------ CU022

    @Test
    void modificarActualizaLosDatosYLasPonderaciones() {
        Pregunta p = pregunta(5L, "VALOR GANADO", "VIEJO", vf, "10", "0");
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));
        when(preguntaDAO.buscarCoincidencias(2L, "VALOR GANADO", "¿EL VALOR GANADO MIDE EL AVANCE?", 5L)).thenReturn(List.of());
        when(opcionRespuestaDAO.buscarPorId(10L)).thenReturn(Optional.of(vf));
        when(preguntaDAO.actualizar(p)).thenReturn(true);
        PreguntaDTO d = datosVF();
        d.setId(5L);
        d.setIdFactor(99L); // la pantalla no deja cambiarlo; si llega otro se ignora
        d.getPonderaciones().get(0).setPonderacion(new BigDecimal("4"));
        d.getPonderaciones().get(1).setPonderacion(new BigDecimal("6"));

        assertTrue(gestor.modificarPregunta(d));

        assertEquals("¿EL VALOR GANADO MIDE EL AVANCE?", p.getEnunciado());
        assertSame(tiempo, p.getFactor());
        assertEquals(2, p.getPonderaciones().size());
        assertEquals(0, new BigDecimal("6").compareTo(p.getPonderacion(vf.getItems().get(1))));
    }

    @Test
    void modificarCambiandoElConjuntoReemplazaLasPonderaciones() {
        Pregunta p = pregunta(5L, "VALOR GANADO", "VIEJO", vf, "10", "0");
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));
        when(preguntaDAO.buscarCoincidencias(any(), any(), any(), any())).thenReturn(List.of());
        when(opcionRespuestaDAO.buscarPorId(20L)).thenReturn(Optional.of(escala));
        when(preguntaDAO.actualizar(p)).thenReturn(true);
        PreguntaDTO d = datosVF();
        d.setId(5L);
        d.setIdOpcionRespuesta(20L);
        d.setPonderaciones(new ArrayList<>());
        for (int i = 0; i < 5; i++) {
            d.getPonderaciones().add(new PonderacionDTO(21L + i, i + 1, null, new BigDecimal("2")));
        }

        assertTrue(gestor.modificarPregunta(d));

        assertSame(escala, p.getOpcionRespuesta());
        assertEquals(5, p.getPonderaciones().size());
        assertTrue(p.getPonderaciones().stream().allMatch(pp -> pp.getItem().getOpcionRespuesta() == escala));
    }

    @Test
    void modificarConDatosBlanqueadosODuplicada() {
        Pregunta p = pregunta(5L, "VALOR GANADO", "VIEJO", vf, "10", "0");
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));
        PreguntaDTO blanqueada = datosVF();
        blanqueada.setId(5L);
        blanqueada.setNombre("");
        blanqueada.setEnunciado(" ");

        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.modificarPregunta(blanqueada));
        assertEquals("No se pudo modificar la pregunta. Corrija lo siguiente:", e.getMessage());
        assertEquals(List.of("Debe ingresar el Nombre de pregunta.", "Debe ingresar la Pregunta."), e.getErrores());

        when(preguntaDAO.buscarCoincidencias(any(), any(), any(), eq(5L)))
                .thenReturn(List.of(pregunta(8L, "LÍNEA BASE", "VALOR GANADO", vf)));
        PreguntaDTO duplicada = datosVF();
        duplicada.setId(5L);
        duplicada.setNombre("LÍNEA BASE");
        PreguntaDuplicadaException d = assertThrows(PreguntaDuplicadaException.class, () -> gestor.modificarPregunta(duplicada));
        assertEquals("No se pudo modificar la pregunta:", d.getMessage());
        verify(preguntaDAO, never()).actualizar(any());
    }

    @Test
    void modificarConOpcionInexistenteOPreguntaDadaDeBaja() {
        Pregunta p = pregunta(5L, "VALOR GANADO", "VIEJO", vf, "10", "0");
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));
        when(preguntaDAO.buscarCoincidencias(any(), any(), any(), any())).thenReturn(List.of());
        when(opcionRespuestaDAO.buscarPorId(10L)).thenReturn(Optional.empty());
        PreguntaDTO d = datosVF();
        d.setId(5L);
        assertThrows(NoSuchElementException.class, () -> gestor.modificarPregunta(d));

        p.darDeBaja(AHORA);
        assertThrows(NoSuchElementException.class, () -> gestor.modificarPregunta(d));
    }

    // ------------------------------------------------------------------ CU023

    @Test
    void darDeBajaMarcaLaFechaYRegistraLaAuditoria() {
        Pregunta p = pregunta(5L, "LÍNEA BASE", "¿...?", vf, "10", "0");
        Consultor jperez = new Consultor("JPEREZ");
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));
        when(consultorDAO.buscarPorUsername("JPEREZ")).thenReturn(Optional.of(jperez));
        when(preguntaDAO.actualizar(p)).thenReturn(true);
        when(gestorAuditoria.registrarBaja("BAJA", "PREGUNTA", 5L, jperez)).thenReturn(true);

        assertTrue(gestor.darDeBajaPregunta(5L, "JPEREZ"));

        assertEquals(AHORA, p.getFechaBaja());
        InOrder orden = inOrder(preguntaDAO, consultorDAO, gestorAuditoria);
        orden.verify(preguntaDAO).buscarPorId(5L);
        orden.verify(consultorDAO).buscarPorUsername("JPEREZ");
        orden.verify(preguntaDAO).actualizar(p);
        orden.verify(gestorAuditoria).registrarBaja("BAJA", "PREGUNTA", 5L, jperez);
    }

    @Test
    void darDeBajaInexistenteOConsultorDesconocido() {
        when(preguntaDAO.buscarPorId(6L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> gestor.darDeBajaPregunta(6L, "JPEREZ"));

        Pregunta p = pregunta(5L, "LÍNEA BASE", "¿...?", vf, "10", "0");
        when(preguntaDAO.buscarPorId(5L)).thenReturn(Optional.of(p));
        when(consultorDAO.buscarPorUsername("NADIE")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> gestor.darDeBajaPregunta(5L, "NADIE"));
        assertNull(p.getFechaBaja());
        verifyNoInteractions(gestorAuditoria);
    }

    // ------------------------------------------------------------------ CU033

    @Test
    void generarConIAPideAlAsistenteConElCatalogo() {
        PreguntaDTO sugerida = new PreguntaDTO(null, 1L, 2L, "N", "E", "D", 10L, new ArrayList<>());
        when(factorDAO.buscarPorId(2L)).thenReturn(Optional.of(tiempo));
        when(opcionRespuestaDAO.buscarTodos()).thenReturn(List.of(vf, escala));
        when(asistenteIA.generarPregunta(gp, tiempo, List.of(vf, escala))).thenReturn(sugerida);

        assertSame(sugerida, gestor.generarPreguntaIA(new SolicitudIADTO(1L, 2L)));
    }

    @Test
    void generarConIASinDatosJuntaLasOmisiones() {
        ValidacionException e = assertThrows(ValidacionException.class, () -> gestor.generarPreguntaIA(new SolicitudIADTO(1L, null)));
        assertEquals("Corrija lo siguiente:", e.getMessage());
        assertEquals(List.of("Debe seleccionar el Factor."), e.getErrores());

        ValidacionException e2 = assertThrows(ValidacionException.class, () -> gestor.generarPreguntaIA(null));
        assertEquals(2, e2.getErrores().size());
        verifyNoInteractions(asistenteIA);
    }

    @Test
    void generarConIAPasaElErrorDelServicioYElFactorInexistente() {
        when(factorDAO.buscarPorId(2L)).thenReturn(Optional.of(tiempo));
        when(factorDAO.buscarPorId(3L)).thenReturn(Optional.empty());
        when(opcionRespuestaDAO.buscarTodos()).thenReturn(List.of(vf));
        when(asistenteIA.generarPregunta(any(), any(), any())).thenThrow(new ServicioIAException("sin servicio"));

        assertThrows(ServicioIAException.class, () -> gestor.generarPreguntaIA(new SolicitudIADTO(1L, 2L)));
        assertThrows(NoSuchElementException.class, () -> gestor.generarPreguntaIA(new SolicitudIADTO(1L, 3L)));
    }
}
