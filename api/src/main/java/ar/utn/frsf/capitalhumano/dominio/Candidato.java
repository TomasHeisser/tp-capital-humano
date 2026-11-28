package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Candidato importado desde el sistema de gestion de candidatos y CV.
 */
@Entity
@Table(name = "candidato")
@DiscriminatorValue("CANDIDATO")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@NoArgsConstructor
public class Candidato extends Usuario {

    @Column(name = "nro_candidato", nullable = false, unique = true)
    private Long nroCandidato;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 3)
    private TipoDocumento tipoDocumento;

    @Column(name = "nro_documento", nullable = false, length = 20)
    private String nroDocumento;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "genero", length = 1)
    private Genero genero;

    @Column(name = "nacionalidad", length = 100)
    private String nacionalidad;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "escolaridad", length = 100)
    private String escolaridad;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @Override
    public Rol getRol() {
        return Rol.CANDIDATO;
    }
}
