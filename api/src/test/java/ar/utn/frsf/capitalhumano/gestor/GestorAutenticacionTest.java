package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.autenticacion.AutenticacionCandidato;
import ar.utn.frsf.capitalhumano.autenticacion.AutenticacionConsultor;
import ar.utn.frsf.capitalhumano.autenticacion.ServicioToken;
import ar.utn.frsf.capitalhumano.dominio.Rol;
import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorAutenticacionTest {

    @Mock
    private AutenticacionConsultor consultor;
    @Mock
    private AutenticacionCandidato candidato;
    @Mock
    private ServicioToken servicioToken;

    private GestorAutenticacion gestor;

    @BeforeEach
    void setUp() {
        gestor = new GestorAutenticacion(consultor, candidato, servicioToken);
    }

    @Test
    void usaLaEstrategiaDelConsultorYLeAgregaElToken() {
        CredencialesDTO cred = new CredencialesDTO();
        cred.setTipoAcceso(Rol.CONSULTOR);
        when(consultor.autenticar(cred)).thenReturn(new SesionDTO(null, Rol.CONSULTOR, "JPEREZ", null));
        when(servicioToken.generarToken("JPEREZ", Rol.CONSULTOR, null)).thenReturn("token");

        SesionDTO s = gestor.autenticar(cred);

        assertEquals("token", s.getToken());
        verifyNoInteractions(candidato);
    }

    @Test
    void usaLaEstrategiaDelCandidato() {
        CredencialesDTO cred = new CredencialesDTO();
        cred.setTipoAcceso(Rol.CANDIDATO);
        when(candidato.autenticar(cred)).thenReturn(new SesionDTO(null, Rol.CANDIDATO, "LÓPEZ, CAMILA", 40L));
        when(servicioToken.generarToken("LÓPEZ, CAMILA", Rol.CANDIDATO, 40L)).thenReturn("t2");

        assertEquals("t2", gestor.autenticar(cred).getToken());
        verifyNoInteractions(consultor);
    }

    @Test
    void sinTipoDeAccesoNoSeSabeQueEstrategiaUsar() {
        assertThrows(ValidacionException.class, () -> gestor.autenticar(new CredencialesDTO()));
        assertThrows(ValidacionException.class, () -> gestor.autenticar(null));
    }
}
