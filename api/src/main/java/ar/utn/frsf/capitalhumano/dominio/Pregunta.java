package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pregunta de un factor, con la ponderacion de cada opcion de su conjunto de opciones.
 */
@Entity
@Table(name = "pregunta")
@Getter
@Setter
@NoArgsConstructor
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "enunciado", nullable = false, length = 500)
    private String enunciado;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_factor", nullable = false)
    private Factor factor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opcion_respuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @OneToMany(mappedBy = "pregunta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PonderacionPreguntaOpcion> ponderaciones = new ArrayList<>();

    public Pregunta(String nombre, String enunciado, String descripcion, Factor factor, OpcionRespuesta opcionRespuesta) {
        this.nombre = nombre;
        this.enunciado = enunciado;
        this.descripcion = descripcion;
        this.factor = factor;
        this.opcionRespuesta = opcionRespuesta;
    }

    public void agregarPonderacion(ItemOpcionRespuesta item, BigDecimal valor) {
        ponderaciones.add(new PonderacionPreguntaOpcion(this, item, valor));
    }

    // baja logica: la fila queda, solo se marca la fecha
    public void darDeBaja(LocalDateTime fecha) {
        this.fechaBaja = fecha;
    }

    // ponderacion que se le dio a un item (cero si no tiene)
    public BigDecimal getPonderacion(ItemOpcionRespuesta item) {
        return ponderaciones.stream()
                .filter(p -> p.getItem().getId().equals(item.getId()))
                .map(PonderacionPreguntaOpcion::getPonderacion)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
