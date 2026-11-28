package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.gestor.GestorEvaluaciones;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "CU025 - Evaluar candidatos")
@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private static final MediaType XLSX = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final GestorEvaluaciones gestor;

    public EvaluacionController(GestorEvaluaciones gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Busca candidatos por apellido, nombre y/o número (al menos un filtro)")
    @GetMapping("/candidatos")
    public List<CandidatoGestionDTO> buscarCandidatos(@ModelAttribute FiltroCandidatoDTO f) {
        return gestor.buscarCandidatos(f);
    }

    @Operation(summary = "Devuelve los candidatos que ya tienen un cuestionario activo o en proceso")
    @PostMapping("/verificacion-activos")
    public List<CandidatoGestionDTO> verificarActivos(@RequestBody List<Long> ids) {
        return gestor.verificarCuestionariosActivos(ids);
    }

    @Operation(summary = "Controla que todas las competencias del puesto sean evaluables y las devuelve con su ponderación")
    @GetMapping("/puestos/{id}/validacion")
    public List<CompetenciaRequeridaDTO> validarPuesto(@PathVariable("id") Long idPuesto) {
        return gestor.validarPuesto(idPuesto);
    }

    @Operation(summary = "Genera una clave de ingreso para cada candidato")
    @PostMapping("/claves")
    public List<CandidatoClaveDTO> generarClaves(@RequestBody List<Long> ids) {
        return gestor.generarClaves(ids);
    }

    @Operation(summary = "Registra la evaluación con la copia del modelo y un cuestionario por candidato")
    @PostMapping
    public EvaluacionDTO registrar(@RequestBody NuevaEvaluacionDTO e) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return gestor.registrarEvaluacion(e, username);
    }

    // el ArchivoDTO del gestor se manda como descarga (.xlsx con su nombre)
    @Operation(summary = "Descarga el Excel con los candidatos de la evaluación y sus claves")
    @GetMapping("/{id}/claves")
    public ResponseEntity<byte[]> exportarClaves(@PathVariable("id") Long idEvaluacion) {
        ArchivoDTO archivo = gestor.exportarClaves(idEvaluacion);
        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(archivo.getNombre(), StandardCharsets.UTF_8).build().toString())
                .body(archivo.getContenido());
    }
}
