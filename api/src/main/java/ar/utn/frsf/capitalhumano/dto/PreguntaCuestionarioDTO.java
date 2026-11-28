package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Pregunta tal como la ve el candidato: enunciado y opciones, sin ponderaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaCuestionarioDTO {

    private Long idPregunta;
    private String enunciado;
    private List<ItemOpcionDTO> opciones;
}
