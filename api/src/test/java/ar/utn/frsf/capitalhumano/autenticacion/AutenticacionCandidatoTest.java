package ar.utn.frsf.capitalhumano.autenticacion;

import ar.utn.frsf.capitalhumano.dao.CandidatoDAO;
import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dominio.Candidato;
import ar.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.utn.frsf.capitalhumano.dominio.Rol;
import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;
import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacionCandidatoTest {

    private static final String MENSAJE = "Los datos ingresados no son válidos o no existe un cuestionario para el Candidato";

    @Mock
    private CandidatoDAO candidatoDAO;
    @Mock
    private CuestionarioDAO cuestionarioDAO;
    @InjectMocks
    private AutenticacionCandidato estrategia;

    private Candidato lopez;
    private Cuestionario cuestionario;

    @BeforeEach
    void setUp() {
        lopez = new Candidato();
        lopez.setId(5L);
        lopez.setApellido("LÓPEZ");
        lopez.setNombre("CAMILA");
        cuestionario = new Cuestionario(lopez, "T6Q9D2LM", 5, 120, 7, 5, LocalDateTime.now());
        cuestionario.setId(40L);
    }

    private CredencialesDTO cred(TipoDocumento tipo, String nro, String clave) {
        CredencialesDTO c = new CredencialesDTO();
        c.setTipoAcceso(Rol.CANDIDATO);
        c.setTipoDocumento(tipo);
        c.setNroDocumento(nro);
        c.setClave(clave);
        return c;
    }

    @Test
    void conDocumentoYClaveCorrectosDevuelveSuCuestionario() {
        when(candidatoDAO.buscarPorDocumento(TipoDocumento.DNI, "40111222")).thenReturn(Optional.of(lopez));
        when(cuestionarioDAO.buscarActivoOEnProceso(5L)).thenReturn(Optional.of(cuestionario));

        SesionDTO s = estrategia.autenticar(cred(TipoDocumento.DNI, "40111222", "t6q9d2lm"));

        assertEquals(Rol.CANDIDATO, s.getRol());
        assertEquals(40L, s.getIdCuestionario());
        assertEquals("LÓPEZ, CAMILA", s.getNombreUsuario());
    }

    @Test
    void claveQueNoEsLaDelCuestionario() {
        when(candidatoDAO.buscarPorDocumento(any(), any())).thenReturn(Optional.of(lopez));
        when(cuestionarioDAO.buscarActivoOEnProceso(5L)).thenReturn(Optional.of(cuestionario));
        var e = assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred(TipoDocumento.DNI, "40111222", "OTRACLAV")));
        assertEquals(MENSAJE, e.getMessage());
    }

    @Test
    void candidatoInexistenteOSinCuestionarioVigente() {
        when(candidatoDAO.buscarPorDocumento(TipoDocumento.DNI, "1")).thenReturn(Optional.empty());
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred(TipoDocumento.DNI, "1", "X")));

        when(candidatoDAO.buscarPorDocumento(TipoDocumento.DNI, "40111222")).thenReturn(Optional.of(lopez));
        when(cuestionarioDAO.buscarActivoOEnProceso(5L)).thenReturn(Optional.empty());
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred(TipoDocumento.DNI, "40111222", "T6Q9D2LM")));
    }

    @Test
    void faltanDatos() {
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred(null, "1", "X")));
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred(TipoDocumento.DNI, " ", "X")));
        assertThrows(ValidacionException.class, () -> estrategia.autenticar(cred(TipoDocumento.DNI, "1", null)));
    }
}
