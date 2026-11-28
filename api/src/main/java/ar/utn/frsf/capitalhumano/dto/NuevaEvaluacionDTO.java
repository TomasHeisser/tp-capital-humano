package ar.utn.frsf.capitalhumano.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que se manda al finalizar Evaluar candidatos: el puesto y los candidatos con sus claves.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NuevaEvaluacionDTO {

    private Long idPuesto;
    private List<CandidatoClaveDTO> candidatos;
}
