package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Puntaje que da una opcion en una pregunta puntual (tabla pondera).
 */
@Entity
@Table(name = "pondera")
@IdClass(PonderacionPreguntaOpcionId.class)
@Getter
@Setter
@NoArgsConstructor
public class PonderacionPreguntaOpcion {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta")
    private Pregunta pregunta;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item")
    private ItemOpcionRespuesta item;

    @Column(name = "ponderacion", nullable = false, precision = 4, scale = 2)
    private BigDecimal ponderacion;

    public PonderacionPreguntaOpcion(Pregunta pregunta, ItemOpcionRespuesta item, BigDecimal ponderacion) {
        this.pregunta = pregunta;
        this.item = item;
        this.ponderacion = ponderacion;
    }
}
