package ar.utn.frsf.capitalhumano.integracion;

import ar.utn.frsf.capitalhumano.dominio.Competencia;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import ar.utn.frsf.capitalhumano.dto.PreguntaDTO;
import ar.utn.frsf.capitalhumano.excepcion.ServicioIAException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdaptadorServicioIATest {

    private final ObjectMapper mapper = JsonMapper.builder().build();
    private APIGemini api;
    private AdaptadorServicioIA adaptador;
    private Competencia gp;
    private Factor tiempo;
    private List<OpcionRespuesta> opciones;

    @BeforeEach
    void setUp() {
        api = mock(APIGemini.class);
        adaptador = new AdaptadorServicioIA(api, mapper);
        gp = new Competencia("COMP-GP", "GESTIÓN DE PROYECTOS", "Planificar y controlar proyectos");
        gp.setId(1L);
        tiempo = new Factor("FAC-GP-TIE", "GESTIÓN DEL TIEMPO", null, 2, gp);
        tiempo.setId(2L);
        OpcionRespuesta vf = new OpcionRespuesta("VERDADERO / FALSO", null);
        vf.setId(10L);
        vf.agregarItem(1, "VERDADERO");
        vf.agregarItem(2, "FALSO");
        OpcionRespuesta escala = new OpcionRespuesta("ESCALA DE 5 NIVELES", null);
        escala.setId(20L);
        escala.agregarItem(1, "MALO");
        opciones = List.of(vf, escala);
    }

    @Test
    void mandaCompetenciaFactorYCatalogoYConvierteLaRespuesta() {
        when(api.generateContent(anyString())).thenReturn("""
                {"nombre":"Uso del diagrama de Gantt","enunciado":"¿Usa diagramas de Gantt?",
                 "descripcion":"Herramientas de planificación","opcionRespuesta":"escala de 5 niveles"}""");

        PreguntaDTO d = adaptador.generarPregunta(gp, tiempo, opciones);

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(api).generateContent(prompt.capture());
        assertTrue(prompt.getValue().contains("GESTIÓN DE PROYECTOS (Planificar y controlar proyectos)"));
        assertTrue(prompt.getValue().contains("GESTIÓN DEL TIEMPO (sin descripción)"));
        assertTrue(prompt.getValue().contains("- VERDADERO / FALSO: VERDADERO, FALSO"));

        assertEquals("USO DEL DIAGRAMA DE GANTT", d.getNombre());
        assertEquals("¿USA DIAGRAMAS DE GANTT?", d.getEnunciado());
        assertEquals("HERRAMIENTAS DE PLANIFICACIÓN", d.getDescripcion());
        assertEquals(20L, d.getIdOpcionRespuesta());
        assertEquals(1L, d.getIdCompetencia());
        assertEquals(2L, d.getIdFactor());
    }

    @Test
    void conjuntoInventadoQuedaSinElegirYTextosLargosSeRecortan() {
        when(api.generateContent(anyString())).thenReturn(
                "{\"nombre\":\"" + "N".repeat(130) + "\",\"enunciado\":\"E\",\"descripcion\":\"\",\"opcionRespuesta\":\"OTRA\"}");

        PreguntaDTO d = adaptador.generarPregunta(gp, tiempo, opciones);

        assertNull(d.getIdOpcionRespuesta());
        assertNull(d.getDescripcion());
        assertEquals(100, d.getNombre().length());
    }

    @Test
    void reconoceElConjuntoAunqueElModeloLeAgregueTextoOCambieLosEspacios() {
        when(api.generateContent(anyString()))
                .thenReturn("{\"nombre\":\"N\",\"enunciado\":\"E\",\"opcionRespuesta\":\"ESCALA DE 5 NIVELES (MALO, REGULAR...)\"}")
                .thenReturn("{\"nombre\":\"N\",\"enunciado\":\"E\",\"opcionRespuesta\":\"Verdadero/Falso\"}")
                .thenReturn("{\"nombre\":\"N\",\"enunciado\":\"E\"}");

        assertEquals(20L, adaptador.generarPregunta(gp, tiempo, opciones).getIdOpcionRespuesta());
        assertEquals(10L, adaptador.generarPregunta(gp, tiempo, opciones).getIdOpcionRespuesta());
        assertNull(adaptador.generarPregunta(gp, tiempo, opciones).getIdOpcionRespuesta());
    }

    @Test
    void respuestaRotaOIncompletaEsErrorDelServicio() {
        when(api.generateContent(anyString())).thenReturn("esto no es json").thenReturn("{\"nombre\":\"X\"}");

        assertThrows(ServicioIAException.class, () -> adaptador.generarPregunta(gp, tiempo, opciones));
        assertThrows(ServicioIAException.class, () -> adaptador.generarPregunta(gp, tiempo, opciones));
    }
}
