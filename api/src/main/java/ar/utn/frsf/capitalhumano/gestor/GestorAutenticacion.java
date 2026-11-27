package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.autenticacion.AutenticacionCandidato;
import ar.utn.frsf.capitalhumano.autenticacion.AutenticacionConsultor;
import ar.utn.frsf.capitalhumano.autenticacion.EstrategiaAutenticacion;
import ar.utn.frsf.capitalhumano.autenticacion.ServicioToken;
import ar.utn.frsf.capitalhumano.dominio.Rol;
import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * CU001: elige la estrategia segun el tipo de acceso (Strategy) y le agrega el token a la sesion.
 */
@Service
public class GestorAutenticacion {

    private final Map<Rol, EstrategiaAutenticacion> estrategias;
    private final ServicioToken servicioToken;

    public GestorAutenticacion(AutenticacionConsultor consultor, AutenticacionCandidato candidato, ServicioToken servicioToken) {
        this.estrategias = Map.of(Rol.CONSULTOR, consultor, Rol.CANDIDATO, candidato);
        this.servicioToken = servicioToken;
    }

    /**
     * Valida las credenciales con la estrategia que corresponde y devuelve la sesion con su token.
     */
    @Transactional
    public SesionDTO autenticar(CredencialesDTO cred) {
        if (cred == null || cred.getTipoAcceso() == null) {
            throw new ValidacionException("Debe seleccionar el tipo de acceso");
        }
        SesionDTO sesion = estrategias.get(cred.getTipoAcceso()).autenticar(cred);
        sesion.setToken(servicioToken.generarToken(sesion.getNombreUsuario(), sesion.getRol(), sesion.getIdCuestionario()));
        return sesion;
    }
}
