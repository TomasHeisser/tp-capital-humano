package ar.utn.frsf.capitalhumano.dominio;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FabricaEstadoCuestionarioTest {

    private final FabricaEstadoCuestionario fabrica = new FabricaEstadoCuestionario();
    private final ConversorEstadoCuestionario conversor = new ConversorEstadoCuestionario();

    @ParameterizedTest
    @CsvSource({"ACTIVO, EstadoActivo", "EN_PROCESO, EstadoEnProceso", "COMPLETO, EstadoCompleto",
            "INCOMPLETO, EstadoIncompleto", "SIN_CONTESTAR, EstadoSinContestar"})
    void armaElEstadoQueCorrespondeAlNombre(String nombre, String clase) {
        EstadoCuestionario e = fabrica.crearEstado(nombre);
        assertEquals(clase, e.getClass().getSimpleName());
        assertEquals(nombre, e.getNombre());
    }

    @Test
    void nombreDesconocidoEsError() {
        assertThrows(IllegalArgumentException.class, () -> fabrica.crearEstado("CUALQUIERA"));
    }

    @Test
    void elConversorVaYVuelve() {
        assertEquals("EN_PROCESO", conversor.convertToDatabaseColumn(new EstadoEnProceso()));
        assertInstanceOf(EstadoCompleto.class, conversor.convertToEntityAttribute("COMPLETO"));
        assertNull(conversor.aColumna(null));
        assertNull(conversor.aEntidad(null));
    }
}
