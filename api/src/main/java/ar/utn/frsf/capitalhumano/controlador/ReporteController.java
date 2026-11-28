package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dominio.TipoReporte;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.gestor.GestorReportes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CU027 y CU028 - Reportes")
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final GestorReportes gestor;

    public ReporteController(GestorReportes gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Busca puestos con la cantidad de candidatos y de evaluaciones completas (al menos un criterio)")
    @GetMapping("/puestos")
    public List<PuestoEstadisticaDTO> buscarPuestos(@ModelAttribute FiltroPuestoDTO f) {
        return gestor.buscarPuestos(f);
    }

    @Operation(summary = "Evaluaciones del puesto (fecha de creación y cantidad de candidatos)")
    @GetMapping("/puestos/{id}/evaluaciones")
    public List<EvaluacionDTO> listarEvaluaciones(@PathVariable("id") Long idPuesto) {
        return gestor.listarEvaluaciones(idPuesto);
    }

    @Operation(summary = "Candidatos con el cuestionario completo en el puesto")
    @GetMapping("/puestos/{id}/evaluados")
    public List<CandidatoEvaluadoDTO> listarEvaluados(@PathVariable("id") Long idPuesto) {
        return gestor.listarEvaluados(idPuesto);
    }

    @Operation(summary = "Emite el reporte: ORDEN_MERITO (idPuesto + idsEvaluaciones) o COMPARATIVO (idCuestionario)")
    @PostMapping("/{tipo}")
    public ReporteDTO emitir(@PathVariable TipoReporte tipo, @RequestBody CriterioReporteDTO criterio) {
        return gestor.emitirReporte(tipo, criterio);
    }
}
