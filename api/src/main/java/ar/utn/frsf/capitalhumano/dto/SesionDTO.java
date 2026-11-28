package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import ar.utn.frsf.capitalhumano.dominio.Rol;

/**
 * Sesion abierta: token JWT, rol y, si es candidato, el cuestionario que tiene que completar.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesionDTO {

    private String token;
    private Rol rol;
    private String nombreUsuario;
    private Long idCuestionario;
}
