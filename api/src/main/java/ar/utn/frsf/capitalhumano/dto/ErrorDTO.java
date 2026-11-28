package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lo que devuelve la API cuando algo sale mal: un mensaje y, si hay, la lista de errores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDTO {

    private String mensaje;
    private List<String> errores;
}
