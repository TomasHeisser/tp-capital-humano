package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Competencia que pide un puesto con su ponderacion minima (tabla requiere).
 */
@Entity
@Table(name = "requiere")
@IdClass(CompetenciaRequeridaId.class)
@Getter
@Setter
@NoArgsConstructor
public class CompetenciaRequerida {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puesto")
    private Puesto puesto;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_competencia")
    private Competencia competencia;

    @Column(name = "ponderacion", nullable = false, precision = 4, scale = 2)
    private BigDecimal ponderacion;

    public CompetenciaRequerida(Puesto puesto, Competencia competencia, BigDecimal ponderacion) {
        this.puesto = puesto;
        this.competencia = competencia;
        this.ponderacion = ponderacion;
    }
}
