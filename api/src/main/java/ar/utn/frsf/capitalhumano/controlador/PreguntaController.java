package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dto.FiltroPreguntaDTO;
import ar.utn.frsf.capitalhumano.dto.PreguntaDTO;
import ar.utn.frsf.capitalhumano.dto.PreguntaResumenDTO;
import ar.utn.frsf.capitalhumano.dto.SolicitudIADTO;
import ar.utn.frsf.capitalhumano.gestor.GestorPreguntas;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CU020 a CU023 y CU033 - Preguntas")
@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    private final GestorPreguntas gestor;

    public PreguntaController(GestorPreguntas gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Busca preguntas activas por competencia, factor y/o nombre (al menos un criterio)")
    @GetMapping
    public List<PreguntaResumenDTO> buscar(@ModelAttribute FiltroPreguntaDTO filtro) {
        return gestor.buscarPreguntas(filtro);
    }

    @Operation(summary = "Devuelve una pregunta con la ponderación de cada opción")
    @GetMapping("/{id}")
    public PreguntaDTO obtener(@PathVariable Long id) {
        return gestor.obtenerPregunta(id);
    }

    @Operation(summary = "Da de alta una pregunta con sus ponderaciones (tienen que sumar 10)")
    @PostMapping
    public boolean registrar(@RequestBody PreguntaDTO p) {
        return gestor.registrarPregunta(p);
    }

    @Operation(summary = "Modifica una pregunta (la competencia y el factor no cambian)")
    @PutMapping("/{id}")
    public boolean modificar(@PathVariable Long id, @RequestBody PreguntaDTO p) {
        p.setId(id);
        return gestor.modificarPregunta(p);
    }

    @Operation(summary = "Baja lógica de una pregunta; queda el registro de auditoría con el consultor")
    @DeleteMapping("/{id}")
    public boolean darDeBaja(@PathVariable Long id) {
        String username = usuarioAutenticado();
        return gestor.darDeBajaPregunta(id, username);
    }

    @Operation(summary = "Propone una pregunta con el asistente de IA para la competencia y el factor elegidos")
    @PostMapping("/ia")
    public PreguntaDTO generarConIA(@RequestBody SolicitudIADTO s) {
        return gestor.generarPreguntaIA(s);
    }

    // el filtro del JWT deja el username del consultor como nombre de la autenticacion
    private String usuarioAutenticado() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
