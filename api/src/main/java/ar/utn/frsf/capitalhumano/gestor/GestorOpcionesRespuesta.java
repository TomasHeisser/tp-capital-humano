package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.OpcionRespuestaDAO;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import ar.utn.frsf.capitalhumano.dto.ItemOpcionDTO;
import ar.utn.frsf.capitalhumano.dto.OpcionRespuestaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Logica de los conjuntos de opciones de respuesta. Por ahora el listado y la consulta
 * que usan el alta y la modificacion de preguntas.
 */
@Service
public class GestorOpcionesRespuesta {

    private final OpcionRespuestaDAO opcionRespuestaDAO;

    public GestorOpcionesRespuesta(OpcionRespuestaDAO opcionRespuestaDAO) {
        this.opcionRespuestaDAO = opcionRespuestaDAO;
    }

    /** Todos los conjuntos activos con sus opciones ordenadas. */
    @Transactional(readOnly = true)
    public List<OpcionRespuestaDTO> listarOpciones() {
        return opcionRespuestaDAO.buscarTodos().stream().map(GestorOpcionesRespuesta::aDTO).toList();
    }

    /** Un conjunto con sus opciones; si no existe tira NoSuchElementException (404). */
    @Transactional(readOnly = true)
    public OpcionRespuestaDTO obtenerOpcion(Long id) {
        return opcionRespuestaDAO.buscarPorId(id)
                .map(GestorOpcionesRespuesta::aDTO)
                .orElseThrow(() -> new NoSuchElementException("La opción de respuesta no existe"));
    }

    static OpcionRespuestaDTO aDTO(OpcionRespuesta o) {
        List<ItemOpcionDTO> items = o.getItems().stream()
                .map(i -> new ItemOpcionDTO(i.getId(), i.getOrden(), i.getDescripcion()))
                .toList();
        return new OpcionRespuestaDTO(o.getId(), o.getNombre(), o.getDescripcion(), items);
    }
}
