package ar.utn.frsf.capitalhumano.autenticacion;

import ar.utn.frsf.capitalhumano.dao.CandidatoDAO;
import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dominio.Candidato;
import ar.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.utn.frsf.capitalhumano.dominio.Rol;
import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.springframework.stereotype.Component;

/**
 * Login del candidato (CU001 3.A): tiene que existir con ese documento, tener un cuestionario
 * Activo o En proceso y la clave tiene que ser la de ese cuestionario.
 */
@Component
public class AutenticacionCandidato implements EstrategiaAutenticacion {

    static final String DATOS_INVALIDOS = "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato";

    private final CandidatoDAO candidatoDAO;
    private final CuestionarioDAO cuestionarioDAO;

    public AutenticacionCandidato(CandidatoDAO candidatoDAO, CuestionarioDAO cuestionarioDAO) {
        this.candidatoDAO = candidatoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
    }

    @Override
    public SesionDTO autenticar(CredencialesDTO cred) {
        if (cred.getTipoDocumento() == null || vacio(cred.getNroDocumento()) || vacio(cred.getClave())) {
            throw new ValidacionException(DATOS_INVALIDOS);
        }
        Candidato candidato = candidatoDAO.buscarPorDocumento(cred.getTipoDocumento(), cred.getNroDocumento())
                .orElseThrow(() -> new ValidacionException(DATOS_INVALIDOS));
        Cuestionario cuestionario = cuestionarioDAO.buscarActivoOEnProceso(candidato.getId())
                .filter(c -> c.getClave().equalsIgnoreCase(cred.getClave().trim()))
                .orElseThrow(() -> new ValidacionException(DATOS_INVALIDOS));
        String nombre = candidato.getApellido() + ", " + candidato.getNombre();
        return new SesionDTO(null, Rol.CANDIDATO, nombre, cuestionario.getId());
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
