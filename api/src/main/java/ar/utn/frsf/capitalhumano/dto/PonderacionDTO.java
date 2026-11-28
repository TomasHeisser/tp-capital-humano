package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Puntaje que le da la pregunta a una opcion de su conjunto.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PonderacionDTO {

    private Long idItem;
    private Integer orden;
    private String descripcion;
    private BigDecimal ponderacion;
}
