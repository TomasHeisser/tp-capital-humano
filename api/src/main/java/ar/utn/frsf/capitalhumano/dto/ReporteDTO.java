package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Encabezado comun de los reportes (puesto, empresa y fecha de emision).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ReporteDTO {

    private String codigoPuesto;
    private String nombrePuesto;
    private String empresa;
    private LocalDateTime fechaEmision;
}
