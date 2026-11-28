package ar.utn.frsf.capitalhumano.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una competencia del comparativo: lo que pide el puesto y lo que obtuvo el candidato.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilaComparativoDTO {

    private String competencia;
    private BigDecimal requerido;
    private BigDecimal obtenido;
}
