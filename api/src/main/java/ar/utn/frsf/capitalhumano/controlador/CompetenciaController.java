package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dto.CompetenciaDTO;
import ar.utn.frsf.capitalhumano.dto.FactorDTO;
import ar.utn.frsf.capitalhumano.gestor.GestorCompetencias;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Competencias y factores")
@RestController
@RequestMapping("/api")
public class CompetenciaController {

    private final GestorCompetencias gestor;

    public CompetenciaController(GestorCompetencias gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Lista las competencias activas")
    @GetMapping("/competencias")
    public List<CompetenciaDTO> listarCompetencias() {
        return gestor.listarCompetencias();
    }

    @Operation(summary = "Lista los factores activos de una competencia")
    @GetMapping("/competencias/{id}/factores")
    public List<FactorDTO> listarFactores(@PathVariable("id") Long idCompetencia) {
        return gestor.listarFactores(idCompetencia);
    }
}
