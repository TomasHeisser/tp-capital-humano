package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluacion de un grupo de candidatos para un puesto. Guarda la copia del modelo
 * (competencias, factores, preguntas y opciones) asi todos los cuestionarios usan el mismo.
 */
@Entity
@Table(name = "evaluacion")
@Getter
@Setter
@NoArgsConstructor
public class Evaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_puesto", nullable = false)
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_consultor", nullable = false)
    private Consultor consultor;

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<CompetenciaEvaluada> competenciasEvaluadas = new ArrayList<>();

    @OneToMany(mappedBy = "evaluacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Cuestionario> cuestionarios = new ArrayList<>();

    public Evaluacion(LocalDateTime fechaCreacion, Puesto puesto, Consultor consultor) {
        this.fechaCreacion = fechaCreacion;
        this.puesto = puesto;
        this.consultor = consultor;
    }

    public void agregarCompetenciaEvaluada(CompetenciaEvaluada ce) {
        ce.setEvaluacion(this);
        competenciasEvaluadas.add(ce);
    }

    public void agregarCuestionario(Cuestionario c) {
        c.setEvaluacion(this);
        cuestionarios.add(c);
    }
}
