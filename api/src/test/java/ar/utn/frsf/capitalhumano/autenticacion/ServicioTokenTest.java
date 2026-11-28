package ar.utn.frsf.capitalhumano.autenticacion;

import ar.utn.frsf.capitalhumano.dominio.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioTokenTest {

    private final ServicioToken servicio = new ServicioToken("", "secreto-de-prueba-para-los-tests", 8);

    @Test
    void elTokenDelConsultorTraeUsuarioYRol() {
        String token = servicio.generarToken("JPEREZ", Rol.CONSULTOR);
        assertTrue(servicio.validarToken(token));
        assertEquals("JPEREZ", servicio.obtenerUsuario(token).orElseThrow());
        assertEquals(Rol.CONSULTOR, servicio.obtenerRol(token).orElseThrow());
        assertTrue(servicio.obtenerIdCuestionario(token).isEmpty());
    }

    @Test
    void elTokenDelCandidatoTraeSuCuestionario() {
        String token = servicio.generarToken("LÓPEZ, CAMILA", Rol.CANDIDATO, 40L);
        assertEquals(40L, servicio.obtenerIdCuestionario(token).orElseThrow());
    }

    @Test
    void tokenFirmadoConOtroSecretoOBasuraNoEsValido() {
        ServicioToken otro = new ServicioToken("otro secreto corto", "no se usa", 8);
        String ajeno = otro.generarToken("JPEREZ", Rol.CONSULTOR);
        assertFalse(servicio.validarToken(ajeno));
        assertFalse(servicio.validarToken("cualquier.cosa.rara"));
        assertFalse(servicio.validarToken(null));
        assertFalse(servicio.validarToken(" "));
    }

    @Test
    void tokenVencidoNoEsValido() {
        ServicioToken sinDuracion = new ServicioToken("", "secreto-de-prueba-para-los-tests", 0);
        assertFalse(sinDuracion.validarToken(sinDuracion.generarToken("JPEREZ", Rol.CONSULTOR)));
    }
}
