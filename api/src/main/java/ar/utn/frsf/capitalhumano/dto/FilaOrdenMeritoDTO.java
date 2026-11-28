package ar.utn.frsf.capitalhumano.dto;

import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una fila del orden de merito (una por cuestionario).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilaOrdenMeritoDTO {

    private Integer posicion;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
    private String apellido;
    private String nombre;
    private String estado;
    private BigDecimal puntaje;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime ultimoIngreso;
    private Integer cantidadAccesos;
}
