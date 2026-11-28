package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Conjunto de opciones de respuesta (por ejemplo VERDADERO / FALSO).
 */
@Entity
@Table(name = "opcion_respuesta")
@Getter
@Setter
@NoArgsConstructor
public class OpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @OneToMany(mappedBy = "opcionRespuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden")
    private List<ItemOpcionRespuesta> items = new ArrayList<>();

    public OpcionRespuesta(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public void agregarItem(Integer orden, String descripcion) {
        items.add(new ItemOpcionRespuesta(this, orden, descripcion));
    }

    public ItemOpcionRespuesta getItem(Long idItem) {
        return items.stream()
                .filter(i -> idItem.equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("La opción " + idItem + " no pertenece a " + nombre));
    }
}
