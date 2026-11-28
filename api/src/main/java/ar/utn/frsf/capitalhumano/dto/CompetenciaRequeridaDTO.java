package ar.utn.frsf.capitalhumano.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Competencia que pide un puesto con su ponderacion minima.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaRequeridaDTO {

    private Long idCompetencia;
    private String nombreCompetencia;
    private BigDecimal ponderacion;
}
