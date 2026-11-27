package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Opcion copiada de una pregunta evaluada, con su ponderacion congelada.
 */
@Entity
@Table(name = "opcion_evaluada")
@Getter
@Setter
@NoArgsConstructor
public class OpcionEvaluada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pregunta_evaluada", nullable = false)
    private PreguntaEvaluada preguntaEvaluada;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "ponderacion", nullable = false, precision = 4, scale = 2)
    private BigDecimal ponderacion;

    public OpcionEvaluada(PreguntaEvaluada preguntaEvaluada, Integer orden, String descripcion, BigDecimal ponderacion) {
        this.preguntaEvaluada = preguntaEvaluada;
        this.orden = orden;
        this.descripcion = descripcion;
        this.ponderacion = ponderacion;
    }
}
