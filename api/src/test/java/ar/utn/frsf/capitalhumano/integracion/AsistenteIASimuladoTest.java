package ar.utn.frsf.capitalhumano.integracion;

import ar.utn.frsf.capitalhumano.dominio.Competencia;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import ar.utn.frsf.capitalhumano.dto.PreguntaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AsistenteIASimuladoTest {

    private Competencia gp;
    private Factor tiempo;
    private OpcionRespuesta vf;
    private OpcionRespuesta escala;

    @BeforeEach
    void setUp() {
        gp = new Competencia("COMP-GP", "GESTIÓN DE PROYECTOS", null);
        gp.setId(1L);
        tiempo = new Factor("FAC-GP-TIE", "GESTIÓN DEL TIEMPO", null, 2, gp);
        tiempo.setId(2L);
        vf = new OpcionRespuesta("VERDADERO / FALSO", null);
        vf.setId(10L);
        escala = new OpcionRespuesta("ESCALA DE 5 NIVELES", null);
        escala.setId(20L);
    }

    @Test
    void armaLaPreguntaConElFactorYElConjuntoDeLaPlantilla() {
        PreguntaDTO d = new AsistenteIASimulado(n -> 0).generarPregunta(gp, tiempo, List.of(vf, escala));

        assertEquals("AUTOEVALUACIÓN EN GESTIÓN DEL TIEMPO", d.getNombre());
        assertTrue(d.getEnunciado().contains("GESTIÓN DE PROYECTOS"));
        assertEquals(1L, d.getIdCompetencia());
        assertEquals(2L, d.getIdFactor());
        assertEquals(20L, d.getIdOpcionRespuesta());
        assertTrue(d.getPonderaciones().isEmpty());
    }

    @Test
    void siNoEstaElConjuntoDeLaPlantillaUsaElPrimeroYSiNoHayNinguno() {
        // la plantilla 3 prefiere VERDADERO / FALSO, que no esta en el catalogo
        assertEquals(20L, new AsistenteIASimulado(n -> 3).generarPregunta(gp, tiempo, List.of(escala)).getIdOpcionRespuesta());
        assertNull(new AsistenteIASimulado(n -> 3).generarPregunta(gp, tiempo, List.of()).getIdOpcionRespuesta());
    }

    @Test
    void recortaLosTextosAlLargoDeLosCampos() {
        tiempo.setNombre("X".repeat(150));
        PreguntaDTO d = new AsistenteIASimulado().generarPregunta(gp, tiempo, List.of(vf));
        assertTrue(d.getNombre().length() <= 100);
        assertTrue(d.getEnunciado().length() <= 500);
    }
}
