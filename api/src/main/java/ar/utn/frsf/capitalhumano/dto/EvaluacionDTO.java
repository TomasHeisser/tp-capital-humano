package ar.utn.frsf.capitalhumano.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una evaluacion: se identifica por la fecha de creacion y la cantidad de candidatos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionDTO {

    private Long id;
    private LocalDateTime fechaCreacion;
    private String nombrePuesto;
    private String empresa;
    private int cantidadCandidatos;
}
