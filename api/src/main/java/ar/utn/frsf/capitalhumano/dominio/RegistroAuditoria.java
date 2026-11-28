package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de quien dio de baja algo y cuando.
 */
@Entity
@Table(name = "registro_auditoria")
@Getter
@Setter
@NoArgsConstructor
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "accion", nullable = false, length = 20)
    private String accion;

    @Column(name = "entidad_afectada", nullable = false, length = 50)
    private String entidadAfectada;

    @Column(name = "id_objeto_afectado", nullable = false)
    private Long idObjetoAfectado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_consultor", nullable = false)
    private Consultor consultor;

    public RegistroAuditoria(LocalDateTime fechaHora, String accion, String entidadAfectada, Long idObjetoAfectado, Consultor consultor) {
        this.fechaHora = fechaHora;
        this.accion = accion;
        this.entidadAfectada = entidadAfectada;
        this.idObjetoAfectado = idObjetoAfectado;
        this.consultor = consultor;
    }
}
