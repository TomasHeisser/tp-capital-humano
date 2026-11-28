package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una fila de la grilla de preguntas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaResumenDTO {

    private Long id;
    private String competencia;
    private String factor;
    private String nombre;
    private String enunciado;
}
