package ar.utn.frsf.capitalhumano.dto;

import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Candidato con la clave de ingreso a su cuestionario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoClaveDTO {

    private Long idCandidato;
    private String apellido;
    private String nombre;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
    private String clave;
}
