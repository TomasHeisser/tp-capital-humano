package ar.utn.frsf.capitalhumano.autenticacion;

import org.junit.jupiter.api.Test;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.Name;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdaptadorLDAPTest {

    private final LdapTemplate ldap = mock(LdapTemplate.class);
    private final AdaptadorLDAP adaptador = new AdaptadorLDAP(ldap, "ou=consultores");

    @Test
    void traduceAUnAuthenticateConElFiltroDelUsuario() {
        when(ldap.authenticate(any(Name.class), eq("(uid=JPEREZ)"), eq("jperez2026"))).thenReturn(true);
        assertTrue(adaptador.validarCredenciales("JPEREZ", "jperez2026"));
    }

    @Test
    void escapaLosCaracteresEspecialesDelFiltro() {
        adaptador.validarCredenciales("A*)(B", "x");
        verify(ldap).authenticate(any(Name.class), eq("(uid=A\\2a\\29\\28B)"), eq("x"));
    }

    @Test
    void siElLdapFallaDevuelveFalse() {
        when(ldap.authenticate(any(Name.class), any(String.class), any(String.class))).thenThrow(new RuntimeException("caido"));
        assertFalse(adaptador.validarCredenciales("JPEREZ", "x"));
    }
}
