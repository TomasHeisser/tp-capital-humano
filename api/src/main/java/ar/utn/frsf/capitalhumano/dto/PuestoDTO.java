package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Fila de la grilla de puestos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuestoDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String empresa;
}
