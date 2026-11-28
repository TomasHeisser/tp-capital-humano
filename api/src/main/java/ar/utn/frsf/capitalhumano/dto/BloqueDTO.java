package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Un paso del wizard con sus preguntas en el orden guardado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloqueDTO {

    private Integer nroBloque;
    private Integer totalBloques;
    private boolean esUltimo;
    private List<PreguntaCuestionarioDTO> preguntas;
}
