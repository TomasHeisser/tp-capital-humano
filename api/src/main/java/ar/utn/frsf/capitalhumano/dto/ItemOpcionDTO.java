package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una opcion dentro de un conjunto (o de una pregunta del cuestionario).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOpcionDTO {

    private Long id;
    private Integer orden;
    private String descripcion;
}
