package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos de una competencia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaDTO {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
}
