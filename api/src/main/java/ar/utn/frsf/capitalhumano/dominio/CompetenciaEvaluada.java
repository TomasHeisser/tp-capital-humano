package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Copia de una competencia del puesto al momento de crear la evaluacion,
 * con la ponderacion minima que se le pidio.
 */
@Entity
@Table(name = "competencia_evaluada")
@Getter
@Setter
@NoArgsConstructor
public class CompetenciaEvaluada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competencia_origen", nullable = false)
    private Competencia competenciaOrigen;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ponderacion_minima", nullable = false, precision = 4, scale = 2)
    private BigDecimal ponderacionMinima;

    @OneToMany(mappedBy = "competenciaEvaluada", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nroOrden")
    private List<FactorEvaluado> factores = new ArrayList<>();

    public CompetenciaEvaluada(Competencia origen, BigDecimal ponderacionMinima) {
        this.competenciaOrigen = origen;
        this.codigo = origen.getCodigo();
        this.nombre = origen.getNombre();
        this.ponderacionMinima = ponderacionMinima;
    }

    public void agregarFactor(FactorEvaluado fe) {
        fe.setCompetenciaEvaluada(this);
        factores.add(fe);
    }
}
