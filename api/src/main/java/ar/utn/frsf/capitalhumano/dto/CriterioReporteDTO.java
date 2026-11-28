package ar.utn.frsf.capitalhumano.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Que reporte se pide: el puesto y las evaluaciones (orden de merito) o el cuestionario (comparativo).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriterioReporteDTO {

    private Long idPuesto;
    private List<Long> idsEvaluaciones;
    private Long idCuestionario;
}
