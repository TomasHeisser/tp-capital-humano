package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Criterios de busqueda de puestos (todos con "contiene").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiltroPuestoDTO {

    private String codigo;
    private String nombre;
    private String empresa;
}
