package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Copia de una pregunta con sus opciones y ponderaciones. Si despues modifican
 * o dan de baja la pregunta original, esta no cambia.
 */
@Entity
@Table(name = "pregunta_evaluada")
@Getter
@Setter
@NoArgsConstructor
public class PreguntaEvaluada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_factor_evaluado", nullable = false)
    private FactorEvaluado factorEvaluado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta_origen", nullable = false)
    private Pregunta preguntaOrigen;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "enunciado", nullable = false, length = 500)
    private String enunciado;

    @OneToMany(mappedBy = "preguntaEvaluada", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden")
    private List<OpcionEvaluada> opciones = new ArrayList<>();

    // copia enunciado y cada opcion con la ponderacion que tenia en la pregunta
    public PreguntaEvaluada(Pregunta origen) {
        this.preguntaOrigen = origen;
        this.nombre = origen.getNombre();
        this.enunciado = origen.getEnunciado();
        origen.getOpcionRespuesta().getItems().forEach(item ->
                opciones.add(new OpcionEvaluada(this, item.getOrden(), item.getDescripcion(), origen.getPonderacion(item))));
    }

    public OpcionEvaluada getOpcion(Long idOpcion) {
        return opciones.stream()
                .filter(o -> idOpcion.equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("La opción " + idOpcion + " no corresponde a la pregunta"));
    }
}
