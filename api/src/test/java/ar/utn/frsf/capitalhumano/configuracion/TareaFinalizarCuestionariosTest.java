package ar.utn.frsf.capitalhumano.configuracion;

import ar.utn.frsf.capitalhumano.gestor.GestorCuestionario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class TareaFinalizarCuestionariosTest {

    private final GestorCuestionario gestor = mock(GestorCuestionario.class);
    private final TareaFinalizarCuestionarios tarea = new TareaFinalizarCuestionarios(gestor);

    @Test
    void cadaCorridaLePideAlGestorQueFinaliceLosVencidos() {
        when(gestor.finalizarVencidos()).thenReturn(3);
        tarea.ejecutar();
        verify(gestor).finalizarVencidos();
    }

    @Test
    void siUnaCorridaFallaNoCortaLaTarea() {
        when(gestor.finalizarVencidos()).thenThrow(new RuntimeException("base caida"));
        assertDoesNotThrow(tarea::ejecutar);
    }
}
