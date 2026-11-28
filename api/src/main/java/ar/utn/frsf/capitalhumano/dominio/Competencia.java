package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Competencia (tecnica o actitudinal) que se compone de factores.
 */
@Entity
@Table(name = "competencia")
@Getter
@Setter
@NoArgsConstructor
public class Competencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @OneToMany(mappedBy = "competencia")
    @OrderBy("nroOrden")
    private List<Factor> factores = new ArrayList<>();

    public Competencia(String codigo, String nombre, String descripcion) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}
