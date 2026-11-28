package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filtros de la busqueda de candidatos (al menos uno).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroCandidatoDTO {

    private String apellido;
    private String nombre;
    private Long nroCandidato;
}
