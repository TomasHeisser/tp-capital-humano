package ar.utn.frsf.capitalhumano.autenticacion;

/**
 * Directorio de usuarios de la organizacion (lo que el sistema espera del LDAP).
 */
public interface IServicioDirectorio {

    boolean validarCredenciales(String usuario, String contrasenia);
}
