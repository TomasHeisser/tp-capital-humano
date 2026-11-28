package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dto.FiltroPuestoDTO;
import ar.utn.frsf.capitalhumano.dto.PuestoDTO;
import ar.utn.frsf.capitalhumano.gestor.GestorPuestos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Puestos")
@RestController
@RequestMapping("/api")
public class PuestoController {

    private final GestorPuestos gestor;

    public PuestoController(GestorPuestos gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Busca puestos por código, nombre y/o empresa (al menos un criterio)")
    @GetMapping("/puestos")
    public List<PuestoDTO> buscar(@ModelAttribute FiltroPuestoDTO filtro) {
        return gestor.buscarPuestos(filtro);
    }
}
