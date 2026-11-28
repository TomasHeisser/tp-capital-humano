package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dto.BloqueDTO;
import ar.utn.frsf.capitalhumano.dto.CuestionarioDTO;
import ar.utn.frsf.capitalhumano.dto.RespuestaDTO;
import ar.utn.frsf.capitalhumano.gestor.GestorCuestionario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// solo el candidato duenio del cuestionario puede llegar aca (lo controla la seguridad)
@Tag(name = "CU026 - Completar cuestionario")
@RestController
@RequestMapping("/api/cuestionarios")
public class CuestionarioController {

    private final GestorCuestionario gestor;

    public CuestionarioController(GestorCuestionario gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Ingreso al cuestionario: cuenta el acceso y devuelve instrucciones, bloque pendiente o el motivo de cierre")
    @PostMapping("/{id}/ingreso")
    public CuestionarioDTO ingresar(@PathVariable("id") Long idCuestionario) {
        return gestor.ingresar(idCuestionario);
    }

    @Operation(summary = "Acepta las instrucciones: sortea las preguntas y devuelve el primer bloque")
    @PostMapping("/{id}/comienzo")
    public BloqueDTO comenzar(@PathVariable("id") Long idCuestionario) {
        return gestor.comenzar(idCuestionario);
    }

    @Operation(summary = "Guarda las respuestas de un bloque y devuelve el siguiente o el cuestionario completo")
    @PostMapping("/{id}/bloques/{nro}")
    public CuestionarioDTO responderBloque(@PathVariable("id") Long idCuestionario, @PathVariable("nro") Integer nroBloque,
                                           @RequestBody List<RespuestaDTO> r) {
        return gestor.responderBloque(idCuestionario, nroBloque, r);
    }
}
