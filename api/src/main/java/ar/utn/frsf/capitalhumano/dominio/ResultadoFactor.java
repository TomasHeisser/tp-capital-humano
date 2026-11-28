package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Puntaje que saco el candidato en un factor (suma de lo elegido / 2).
 */
@Entity
@Table(name = "resultado_factor")
@IdClass(ResultadoFactorId.class)
@Getter
@Setter
@NoArgsConstructor
public class ResultadoFactor {

    @Id
    @Column(name = "id_cuestionario")
    private Long idCuestionario;

    @Id
    @Column(name = "id_factor_evaluado")
    private Long idFactorEvaluado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", insertable = false, updatable = false)
    private Cuestionario cuestionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_factor_evaluado", insertable = false, updatable = false)
    private FactorEvaluado factorEvaluado;

    @Column(name = "puntaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntaje;

    public ResultadoFactor(FactorEvaluado factorEvaluado, BigDecimal puntaje) {
        this.factorEvaluado = factorEvaluado;
        this.idFactorEvaluado = factorEvaluado.getId();
        this.puntaje = puntaje;
    }

    void asignarCuestionario(Cuestionario c) {
        this.cuestionario = c;
        this.idCuestionario = c.getId();
    }
}
