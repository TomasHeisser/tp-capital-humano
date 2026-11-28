package ar.utn.frsf.capitalhumano.autenticacion;

import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;

/**
 * Strategy del login: cada tipo de acceso valida las credenciales a su manera.
 * Devuelve la sesion sin token; el token lo pone el GestorAutenticacion.
 */
public interface EstrategiaAutenticacion {

    SesionDTO autenticar(CredencialesDTO cred);
}
