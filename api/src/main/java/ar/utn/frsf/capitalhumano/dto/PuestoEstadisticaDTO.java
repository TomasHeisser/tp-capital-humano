package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Puesto con la cantidad de candidatos y de evaluaciones completas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuestoEstadisticaDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String empresa;
    private long cantidadCandidatos;
    private long evaluacionesCompletas;
}
