package ar.utn.frsf.capitalhumano.dto;

import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Candidato con el cuestionario completo en un puesto (para el reporte comparativo).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidatoEvaluadoDTO {

    private Long idCuestionario;
    private String apellido;
    private String nombre;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
    private Long nroCandidato;
    private LocalDateTime fechaFin;
}
