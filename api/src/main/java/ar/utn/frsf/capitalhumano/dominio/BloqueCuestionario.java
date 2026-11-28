package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ListIndexBase;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Paso del wizard: un grupo de preguntas en el orden en que se sortearon (tabla asigna).
 * Es debil de Cuestionario, por eso la clave es cuestionario + nro de bloque.
 */
@Entity
@Table(name = "bloque_cuestionario")
@IdClass(BloqueCuestionarioId.class)
@Getter
@Setter
@NoArgsConstructor
public class BloqueCuestionario {

    @Id
    @Column(name = "id_cuestionario")
    private Long idCuestionario;

    @Id
    @Column(name = "nro_bloque")
    private Integer nroBloque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuestionario", insertable = false, updatable = false)
    private Cuestionario cuestionario;

    @Column(name = "respondido", nullable = false)
    private Boolean respondido = false;

    // el orden de la lista es el campo orden de asigna (arranca en 1)
    @ManyToMany
    @JoinTable(name = "asigna",
            joinColumns = {
                    @JoinColumn(name = "id_cuestionario", referencedColumnName = "id_cuestionario"),
                    @JoinColumn(name = "nro_bloque", referencedColumnName = "nro_bloque")},
            inverseJoinColumns = @JoinColumn(name = "id_pregunta_evaluada"))
    @OrderColumn(name = "orden")
    @ListIndexBase(1)
    private List<PreguntaEvaluada> preguntas = new ArrayList<>();

    @OneToMany(mappedBy = "bloque", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RespuestaCandidato> respuestas = new ArrayList<>();

    public BloqueCuestionario(Integer nroBloque, List<PreguntaEvaluada> preguntas) {
        this.nroBloque = nroBloque;
        this.preguntas.addAll(preguntas);
    }

    void asignarCuestionario(Cuestionario c) {
        this.cuestionario = c;
        this.idCuestionario = c.getId();
    }

    public void registrarRespuesta(PreguntaEvaluada p, OpcionEvaluada o, LocalDateTime ahora) {
        respuestas.add(new RespuestaCandidato(this, p, o, ahora));
        respondido = estaCompleto();
    }

    // completo = cada pregunta del bloque tiene su respuesta
    public boolean estaCompleto() {
        return preguntas.stream().allMatch(p -> respuestas.stream()
                .anyMatch(r -> r.getPreguntaEvaluada().getId().equals(p.getId())));
    }

    public boolean estaRespondido() {
        return Boolean.TRUE.equals(respondido);
    }
}
