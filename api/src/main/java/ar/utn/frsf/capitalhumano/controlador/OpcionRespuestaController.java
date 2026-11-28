package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dto.OpcionRespuestaDTO;
import ar.utn.frsf.capitalhumano.gestor.GestorOpcionesRespuesta;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Opciones de respuesta")
@RestController
@RequestMapping("/api/opciones-respuesta")
public class OpcionRespuestaController {

    private final GestorOpcionesRespuesta gestor;

    public OpcionRespuestaController(GestorOpcionesRespuesta gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Lista los conjuntos de opciones de respuesta activos con sus opciones")
    @GetMapping
    public List<OpcionRespuestaDTO> listarOpciones() {
        return gestor.listarOpciones();
    }

    @Operation(summary = "Devuelve un conjunto de opciones de respuesta")
    @GetMapping("/{id}")
    public OpcionRespuestaDTO obtener(@PathVariable Long id) {
        return gestor.obtenerOpcion(id);
    }
}
