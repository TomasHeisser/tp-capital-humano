package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Consultor de la consultora. La contrasenia la valida el LDAP, aca solo guardamos el username.
 */
@Entity
@Table(name = "consultor")
@DiscriminatorValue("CONSULTOR")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Consultor extends Usuario {

    @Column(name = "username", nullable = false, unique = true, length = 15)
    private String username;

    public Consultor(String username) {
        this.username = username;
    }

    @Override
    public Rol getRol() {
        return Rol.CONSULTOR;
    }
}
