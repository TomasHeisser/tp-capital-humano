package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import ar.utn.frsf.capitalhumano.dominio.Rol;
import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;

/**
 * Datos del login. El consultor manda usuario y contrasenia; el candidato tipo y nro de documento y clave.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CredencialesDTO {

    private Rol tipoAcceso;
    private String usuario;
    private String contrasenia;
    private TipoDocumento tipoDocumento;
    private String nroDocumento;
    private String clave;
}
