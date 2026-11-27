package ar.utn.frsf.capitalhumano.autenticacion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

/**
 * Adapter: traduce validarCredenciales a un bind contra el LDAP usando LdapTemplate.
 */
@Slf4j
@Component
public class AdaptadorLDAP implements IServicioDirectorio {

    private final LdapTemplate ldap;
    private final String baseConsultores;

    public AdaptadorLDAP(LdapTemplate ldap, @Value("${ldap.base-consultores}") String baseConsultores) {
        this.ldap = ldap;
        this.baseConsultores = baseConsultores;
    }

    @Override
    public boolean validarCredenciales(String usuario, String contrasenia) {
        try {
            // el filtro va escapado para que no se pueda meter nada raro en el usuario
            String filtro = "(uid=" + LdapEncoder.filterEncode(usuario) + ")";
            return ldap.authenticate(LdapUtils.newLdapName(baseConsultores), filtro, contrasenia);
        } catch (RuntimeException e) {
            log.warn("No se pudo validar contra el LDAP: {}", e.getMessage());
            return false;
        }
    }
}
