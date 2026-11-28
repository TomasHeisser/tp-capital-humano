package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Opcion que eligio el candidato para una pregunta de un bloque (relacion ternaria responde).
 */
@Entity
@Table(name = "responde")
@IdClass(RespuestaCandidatoId.class)
@Getter
@Setter
@NoArgsConstructor
public class RespuestaCandidato {

    @Id
    @Column(name = "id_cuestionario")
    private Long idCuestionario;

    @Id
    @Column(name = "nro_bloque")
    private Integer nroBloque;

    @Id
    @Column(name = "id_pregunta_evaluada")
    private Long idPreguntaEvaluada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "id_cuestionario", referencedColumnName = "id_cuestionario", insertable = false, updatable = false),
            @JoinColumn(name = "nro_bloque", referencedColumnName = "nro_bloque", insertable = false, updatable = false)})
    private BloqueCuestionario bloque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta_evaluada", insertable = false, updatable = false)
    private PreguntaEvaluada preguntaEvaluada;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_opcion_evaluada", nullable = false)
    private OpcionEvaluada opcionEvaluada;

    @Column(name = "fecha_respuesta", nullable = false)
    private LocalDateTime fechaRespuesta;

    public RespuestaCandidato(BloqueCuestionario bloque, PreguntaEvaluada pregunta, OpcionEvaluada opcion, LocalDateTime ahora) {
        this.bloque = bloque;
        this.idCuestionario = bloque.getIdCuestionario();
        this.nroBloque = bloque.getNroBloque();
        this.preguntaEvaluada = pregunta;
        this.idPreguntaEvaluada = pregunta.getId();
        this.opcionEvaluada = opcion;
        this.fechaRespuesta = ahora;
    }
}
