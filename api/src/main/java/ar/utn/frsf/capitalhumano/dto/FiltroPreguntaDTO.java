package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda de preguntas (todos opcionales, pero tiene que venir al menos uno).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPreguntaDTO {

    private Long idCompetencia;
    private Long idFactor;
    private String nombre;
}
