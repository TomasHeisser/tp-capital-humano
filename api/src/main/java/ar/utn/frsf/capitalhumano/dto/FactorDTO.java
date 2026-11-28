package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos de un factor con su competencia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactorDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer nroOrden;
    private Long idCompetencia;
    private String nombreCompetencia;
}
