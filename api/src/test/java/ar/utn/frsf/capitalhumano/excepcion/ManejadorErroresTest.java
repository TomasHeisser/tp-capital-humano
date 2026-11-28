package ar.utn.frsf.capitalhumano.excepcion;

import ar.utn.frsf.capitalhumano.dto.ErrorDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ManejadorErroresTest {

    private final ManejadorErrores manejador = new ManejadorErrores();

    @Test
    void cadaErrorDeNegocioConSuCodigo() {
        assertEquals(400, manejador.manejar(new ValidacionException("x")).getStatusCode().value());
        assertEquals(400, manejador.manejar(new RespuestasIncompletasException("x")).getStatusCode().value());
        assertEquals(409, manejador.manejar(new PreguntaDuplicadaException("x")).getStatusCode().value());
        assertEquals(422, manejador.manejar(new CompetenciasNoEvaluablesException("x")).getStatusCode().value());
        assertEquals(503, manejador.manejar(new ServicioIAException("x")).getStatusCode().value());
        assertEquals(422, manejador.manejar(new NegocioException("x")).getStatusCode().value());
    }

    @Test
    void elCuerpoTraeMensajeYTodosLosErrores() {
        ResponseEntity<ErrorDTO> r = manejador.manejar(
                new ValidacionException("No se pudo registrar la pregunta. Corrija lo siguiente:", List.of("Falta el nombre", "Falta el factor")));
        assertEquals("No se pudo registrar la pregunta. Corrija lo siguiente:", r.getBody().getMensaje());
        assertEquals(2, r.getBody().getErrores().size());
    }

    @Test
    void otrosErrores() {
        assertEquals(404, manejador.noEncontrado(new NoSuchElementException()).getStatusCode().value());
        assertEquals("El dato buscado no existe", manejador.noEncontrado(new NoSuchElementException()).getBody().getMensaje());
        assertEquals("No existe", manejador.noEncontrado(new NoSuchElementException("No existe")).getBody().getMensaje());
        assertEquals(422, manejador.estadoInvalido(new IllegalStateException("x")).getStatusCode().value());
        assertEquals(400, manejador.pedidoMalArmado(new RuntimeException()).getStatusCode().value());
        assertEquals(500, manejador.inesperado(new RuntimeException()).getStatusCode().value());
    }
}
