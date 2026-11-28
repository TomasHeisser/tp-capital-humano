package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cada opcion dentro de un conjunto (orden de visualizacion + descripcion).
 */
@Entity
@Table(name = "item_opcion")
@Getter
@Setter
@NoArgsConstructor
public class ItemOpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opcion_respuesta", nullable = false)
    private OpcionRespuesta opcionRespuesta;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    public ItemOpcionRespuesta(OpcionRespuesta opcionRespuesta, Integer orden, String descripcion) {
        this.opcionRespuesta = opcionRespuesta;
        this.orden = orden;
        this.descripcion = descripcion;
    }
}
