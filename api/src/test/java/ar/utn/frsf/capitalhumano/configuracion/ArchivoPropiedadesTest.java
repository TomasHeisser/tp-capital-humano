package ar.utn.frsf.capitalhumano.configuracion;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * application.properties se lee como ISO-8859-1: las tildes tienen que estar escritas como \\uXXXX
 * para que las instrucciones del cuestionario no salgan con caracteres raros.
 */
class ArchivoPropiedadesTest {

    @Test
    void lasInstruccionesSeLeenConTildes() throws IOException {
        Properties p = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            p.load(in);
        }
        String instrucciones = p.getProperty("cuestionario.instrucciones");
        assertTrue(instrucciones.startsWith("El cuestionario está formado por preguntas de opción múltiple"));
        assertFalse(instrucciones.contains("Ã"));
        assertEquals("5", p.getProperty("cuestionario.preguntas-por-bloque"));
    }
}
