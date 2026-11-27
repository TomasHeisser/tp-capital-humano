package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Copia de un factor evaluable (2 o mas preguntas activas) con sus preguntas.
 */
@Entity
@Table(name = "factor_evaluado")
@Getter
@Setter
@NoArgsConstructor
public class FactorEvaluado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_competencia_evaluada", nullable = false)
    private CompetenciaEvaluada competenciaEvaluada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_factor_origen", nullable = false)
    private Factor factorOrigen;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "nro_orden", nullable = false)
    private Integer nroOrden;

    @OneToMany(mappedBy = "factorEvaluado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<PreguntaEvaluada> preguntas = new ArrayList<>();

    public FactorEvaluado(Factor origen) {
        this.factorOrigen = origen;
        this.codigo = origen.getCodigo();
        this.nombre = origen.getNombre();
        this.nroOrden = origen.getNroOrden();
    }

    public void agregarPregunta(PreguntaEvaluada pe) {
        pe.setFactorEvaluado(this);
        preguntas.add(pe);
    }
}
