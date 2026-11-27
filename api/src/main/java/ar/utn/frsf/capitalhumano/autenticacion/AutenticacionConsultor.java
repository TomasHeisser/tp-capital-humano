package ar.utn.frsf.capitalhumano.autenticacion;

import ar.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.utn.frsf.capitalhumano.dominio.Consultor;
import ar.utn.frsf.capitalhumano.dominio.Rol;
import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.springframework.stereotype.Component;

/**
 * Login del consultor: usuario y contrasenia contra el LDAP. Si es su primer ingreso
 * se lo guarda en la base (solo el username).
 */
@Component
public class AutenticacionConsultor implements EstrategiaAutenticacion {

    static final String CREDENCIALES_INVALIDAS = "El usuario o la contraseña no son válidos";
    private static final int MAX_USUARIO = 15;
    private static final int MAX_CONTRASENIA = 20;

    private final IServicioDirectorio directorio;
    private final ConsultorDAO consultorDAO;

    public AutenticacionConsultor(IServicioDirectorio directorio, ConsultorDAO consultorDAO) {
        this.directorio = directorio;
        this.consultorDAO = consultorDAO;
    }

    @Override
    public SesionDTO autenticar(CredencialesDTO cred) {
        String usuario = cred.getUsuario() == null ? "" : cred.getUsuario().trim().toUpperCase();
        String contrasenia = cred.getContrasenia() == null ? "" : cred.getContrasenia();
        // vacio o mas largo de lo que admite el LDAP: ni lo consultamos
        if (usuario.isEmpty() || contrasenia.isEmpty() || usuario.length() > MAX_USUARIO
                || contrasenia.length() > MAX_CONTRASENIA || !directorio.validarCredenciales(usuario, contrasenia)) {
            throw new ValidacionException(CREDENCIALES_INVALIDAS);
        }
        Consultor consultor = consultorDAO.buscarPorUsername(usuario).orElseGet(() -> {
            Consultor nuevo = new Consultor(usuario);
            consultorDAO.guardar(nuevo);
            return nuevo;
        });
        return new SesionDTO(null, Rol.CONSULTOR, consultor.getUsername(), null);
    }
}
