package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Inicio o fin de cada corrida de la tarea que finaliza cuestionarios vencidos.
 */
@Entity
@Table(name = "registro_ejecucion")
@Getter
@Setter
@NoArgsConstructor
public class RegistroEjecucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "accion", nullable = false, length = 10)
    private AccionEjecucion accion;

    public RegistroEjecucion(LocalDateTime fechaHora, AccionEjecucion accion) {
        this.fechaHora = fechaHora;
        this.accion = accion;
    }
}
