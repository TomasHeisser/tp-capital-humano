package ar.utn.frsf.capitalhumano.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte comparativo de un candidato: requerido vs obtenido en cada competencia.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReporteComparativoDTO extends ReporteDTO {

    private String candidato;
    private List<FilaComparativoDTO> filas;

    public ReporteComparativoDTO(String codigoPuesto, String nombrePuesto, String empresa, String candidato,
                                 List<FilaComparativoDTO> filas, LocalDateTime fechaEmision) {
        super(codigoPuesto, nombrePuesto, empresa, fechaEmision);
        this.candidato = candidato;
        this.filas = filas;
    }
}
