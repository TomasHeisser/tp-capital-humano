package ar.utn.frsf.capitalhumano.dto;

import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un candidato en las grillas de Evaluar candidatos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoGestionDTO {

    private Long id;
    private Long nroCandidato;
    private String apellido;
    private String nombre;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
}
