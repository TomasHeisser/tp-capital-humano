package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que ve el candidato al entrar o al responder: estado, mensaje (si termino o vencio), instrucciones o bloque actual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuestionarioDTO {

    private Long id;
    private String estado;
    private String mensaje;
    private String instrucciones;
    private String nombrePuesto;
    private BloqueDTO bloqueActual;
}
