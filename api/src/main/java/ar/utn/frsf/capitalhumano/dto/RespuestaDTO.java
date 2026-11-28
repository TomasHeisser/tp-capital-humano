package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Opcion que eligio el candidato en una pregunta del bloque.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaDTO {

    private Long idPregunta;
    private Long idOpcion;
}
