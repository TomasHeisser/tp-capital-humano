package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Factor de una competencia; agrupa las preguntas.
 */
@Entity
@Table(name = "factor")
@Getter
@Setter
@NoArgsConstructor
public class Factor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competencia", nullable = false)
    private Competencia competencia;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @OneToMany(mappedBy = "factor")
    private List<Pregunta> preguntas = new ArrayList<>();

    public Factor(String codigo, String nombre, String descripcion, Integer nroOrden, Competencia competencia) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nroOrden = nroOrden;
        this.competencia = competencia;
    }

    // las dadas de baja no cuentan (ni para evaluar ni para copiar el modelo)
    public List<Pregunta> getPreguntasActivas() {
        return preguntas.stream()
                .filter(p -> p.getFechaBaja() == null)
                .toList();
    }
}
