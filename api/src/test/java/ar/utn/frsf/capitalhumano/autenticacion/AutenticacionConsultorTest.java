package ar.utn.frsf.capitalhumano.autenticacion;

import ar.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.utn.frsf.capitalhumano.dominio.Consultor;
import ar.utn.frsf.capitalhumano.dominio.Rol;
import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacionConsultorTest {

    @Mock
    private IServicioDirectorio directorio;
    @Mock
    private ConsultorDAO consultorDAO;
    @InjectMocks
    private AutenticacionConsultor estrategia;

    private CredencialesDTO cred(String usuario, String contrasenia) {
        CredencialesDTO c = new CredencialesDTO();
        c.setTipoAcceso(Rol.CONSULTOR);
        c.setUsuario(usuario);
        c.setContrasenia(contrasenia);
        return c;
    }

    @Test
    void consultorConocidoEntraSinVolverAGuardarse() {
        when(directorio.validarCredenciales("JPEREZ", "jperez2026")).thenReturn(true);
        when(consultorDAO.buscarPorUsername("JPEREZ")).thenReturn(Optional.of(new Consultor("JPEREZ")));

        SesionDTO s = estrategia.autenticar(cred("jperez", "jperez2026"));

        assertEquals(Rol.CONSULTOR, s.getRol());
        assertEquals("JPEREZ", s.getNombreUsuario());
        assertNull(s.getIdCuestionario());
        verify(consultorDAO, never()).guardar(any());
    }

    @Test
    void enSuPrimerIngresoSeGuardaElUsername() {
        when(directorio.validarCredenciales("MGARCIA", "mgarcia2026")).thenReturn(true);
        when(consultorDAO.buscarPorUsername("MGARCIA")).thenReturn(Optional.empty());

        estrategia.autenticar(cred("MGARCIA", "mgarcia2026"));

        verify(consultorDAO).guardar(argThat(c -> c.getUsername().equals("MGARCIA")));
    }

    @Test
    void contraseniaIncorrectaDaElMensajeDelCasoDeUso() {
        when(directorio.validarCredenciales(anyString(), anyString())).thenReturn(false);
        var e = assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred("JPEREZ", "mal")));
        assertEquals("El usuario o la contraseña no son válidos", e.getMessage());
    }

    @Test
    void datosVaciosOLargosNiSeConsultanEnElLdap() {
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred(null, "x")));
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred("JPEREZ", null)));
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred("USUARIOMUYLARGO16", "x")));
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred("JPEREZ", "x".repeat(21))));
        verifyNoInteractions(directorio);
    }
}
