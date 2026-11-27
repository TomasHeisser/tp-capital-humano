package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Conjunto de opciones de respuesta con sus items.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpcionRespuestaDTO {

    private Long id;
    private String nombre;
    private String descripcion;
    private List<ItemOpcionDTO> items;
}
