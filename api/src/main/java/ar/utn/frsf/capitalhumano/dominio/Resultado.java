package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Puntaje que saco el candidato en una competencia (tabla obtiene_resultado).
 */
@Entity
@Table(name = "obtiene_resultado")
@IdClass(ResultadoId.class)
@Getter
@Setter
@NoArgsConstructor
public class Resultado {

    @Id
    @Column(name = "id_cuestionario")
    private Long idCuestionario;

    @Id
    @Column(name = "id_competencia_evaluada")
    private Long idCompetenciaEvaluada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", insertable = false, updatable = false)
    private Cuestionario cuestionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competencia_evaluada", insertable = false, updatable = false)
    private CompetenciaEvaluada competenciaEvaluada;

    @Column(name = "puntaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal puntaje;

    // el detalle por factor se guarda en resultado_factor (lo cuelga el cuestionario)
    @Transient
    @Getter(AccessLevel.NONE)
    private List<ResultadoFactor> resultadosFactor = new ArrayList<>();

    public Resultado(CompetenciaEvaluada competenciaEvaluada, BigDecimal puntaje, List<ResultadoFactor> resultadosFactor) {
        this.competenciaEvaluada = competenciaEvaluada;
        this.idCompetenciaEvaluada = competenciaEvaluada.getId();
        this.puntaje = puntaje;
        this.resultadosFactor.addAll(resultadosFactor);
    }

    void asignarCuestionario(Cuestionario c) {
        this.cuestionario = c;
        this.idCuestionario = c.getId();
    }

    // si se leyo de la base, el detalle se busca en el cuestionario
    public List<ResultadoFactor> getResultadosFactor() {
        if (!resultadosFactor.isEmpty() || cuestionario == null) {
            return resultadosFactor;
        }
        return cuestionario.getResultadosFactor().stream()
                .filter(rf -> rf.getFactorEvaluado().getCompetenciaEvaluada().getId().equals(idCompetenciaEvaluada))
                .toList();
    }
}
