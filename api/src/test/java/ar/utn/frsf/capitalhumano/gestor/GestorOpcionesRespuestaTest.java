package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.OpcionRespuestaDAO;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import ar.utn.frsf.capitalhumano.dto.OpcionRespuestaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorOpcionesRespuestaTest {

    @Mock
    private OpcionRespuestaDAO opcionRespuestaDAO;
    @InjectMocks
    private GestorOpcionesRespuesta gestor;

    private OpcionRespuesta verdaderoFalso() {
        OpcionRespuesta o = new OpcionRespuesta("VERDADERO / FALSO", null);
        o.setId(1L);
        o.agregarItem(1, "VERDADERO");
        o.agregarItem(2, "FALSO");
        return o;
    }

    @Test
    void listaLosConjuntosConSusOpciones() {
        when(opcionRespuestaDAO.buscarTodos()).thenReturn(List.of(verdaderoFalso()));
        List<OpcionRespuestaDTO> r = gestor.listarOpciones();
        assertEquals(1, r.size());
        assertEquals("FALSO", r.get(0).getItems().get(1).getDescripcion());
        assertEquals(2, r.get(0).getItems().get(1).getOrden());
    }

    @Test
    void obtenerUnoExistenteOFallaCon404() {
        when(opcionRespuestaDAO.buscarPorId(1L)).thenReturn(Optional.of(verdaderoFalso()));
        when(opcionRespuestaDAO.buscarPorId(9L)).thenReturn(Optional.empty());
        assertEquals("VERDADERO / FALSO", gestor.obtenerOpcion(1L).getNombre());
        assertThrows(NoSuchElementException.class, () -> gestor.obtenerOpcion(9L));
    }
}
