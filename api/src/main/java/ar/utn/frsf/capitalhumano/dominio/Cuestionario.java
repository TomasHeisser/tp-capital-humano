package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cuestionario de un candidato dentro de una evaluacion.
 * Lo que puede hacer depende del estado en que esta (patron State): cada metodo le delega al estado.
 */
@Entity
@Table(name = "cuestionario")
@Getter
@Setter
@NoArgsConstructor
public class Cuestionario {

    // una sola fabrica para todos, los estados no guardan nada adentro
    private static final FabricaEstadoCuestionario FABRICA = new FabricaEstadoCuestionario();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private Evaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_candidato", nullable = false)
    private Candidato candidato;

    @Column(name = "clave", nullable = false, length = 8)
    private String clave;

    // en la base queda el nombre del estado; el conversor arma el objeto con la fabrica
    @Convert(converter = ConversorEstadoCuestionario.class)
    @Column(name = "estado", nullable = false, length = 20)
    @Setter(AccessLevel.NONE)
    private EstadoCuestionario estado;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "ultimo_ingreso")
    private LocalDateTime ultimoIngreso;

    @Column(name = "fecha_cambio_estado", nullable = false)
    private LocalDateTime fechaCambioEstado;

    @Column(name = "cantidad_accesos", nullable = false)
    private Integer cantidadAccesos;

    @Column(name = "preguntas_por_bloque", nullable = false)
    private Integer preguntasPorBloque;

    @Column(name = "tiempo_maximo_min", nullable = false)
    private Integer tiempoMaximoMinutos;

    @Column(name = "vigencia_dias", nullable = false)
    private Integer vigenciaDias;

    @Column(name = "max_accesos", nullable = false)
    private Integer maxAccesos;

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("nroBloque")
    private List<BloqueCuestionario> bloques = new ArrayList<>();

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resultado> resultados = new ArrayList<>();

    @OneToMany(mappedBy = "cuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoFactor> resultadosFactor = new ArrayList<>();

    // los parametros se copian del repositorio de configuracion al crearlo; arranca Activo
    public Cuestionario(Candidato candidato, String clave, Integer preguntasPorBloque, Integer tiempoMaximoMinutos,
                        Integer vigenciaDias, Integer maxAccesos, LocalDateTime ahora) {
        this.candidato = candidato;
        this.clave = clave;
        this.preguntasPorBloque = preguntasPorBloque;
        this.tiempoMaximoMinutos = tiempoMaximoMinutos;
        this.vigenciaDias = vigenciaDias;
        this.maxAccesos = maxAccesos;
        this.fechaCreacion = ahora;
        this.cantidadAccesos = 0;
        cambiarEstado(EstadoCuestionario.ACTIVO, ahora);
    }

    /** Cuenta el ingreso y dice si el candidato puede seguir (si no, el estado ya lo paso a donde corresponde). */
    public boolean registrarAcceso(LocalDateTime ahora) {
        return estado.registrarAcceso(this, ahora);
    }

    /** Guarda los bloques sorteados y arranca a correr el tiempo. */
    public void iniciar(List<BloqueCuestionario> bloques, LocalDateTime ahora) {
        estado.iniciar(this, ahora);
        bloques.forEach(b -> b.asignarCuestionario(this));
        this.bloques.addAll(bloques);
    }

    /** Guarda los resultados calculados y lo da por completo. */
    public void completar(List<Resultado> resultados, LocalDateTime ahora) {
        estado.completar(this, ahora);
        resultados.forEach(r -> {
            r.asignarCuestionario(this);
            this.resultados.add(r);
            r.getResultadosFactor().forEach(rf -> {
                rf.asignarCuestionario(this);
                this.resultadosFactor.add(rf);
            });
        });
    }

    /** Lo usa la tarea de vencimientos: true si cambio de estado. */
    public boolean verificarVencimiento(LocalDateTime ahora) {
        return estado.verificarVencimiento(this, ahora);
    }

    public void cambiarEstado(String nombreEstado, LocalDateTime ahora) {
        this.estado = FABRICA.crearEstado(nombreEstado);
        this.fechaCambioEstado = ahora;
    }

    /** Primer bloque sin responder, o null si ya respondio todos. */
    public BloqueCuestionario getBloquePendiente() {
        return bloques.stream()
                .filter(b -> !b.estaRespondido())
                .findFirst()
                .orElse(null);
    }

    // ---- chequeos de si mismo que usan los estados ----

    public boolean superoMaxAccesos() {
        return cantidadAccesos > maxAccesos;
    }

    // tiempo maximo: se cuenta desde que acepto las instrucciones (fechaInicio)
    public boolean superoTiempoMaximo(LocalDateTime ahora) {
        return fechaInicio != null && ahora.isAfter(fechaInicio.plusMinutes(tiempoMaximoMinutos));
    }

    // vigencia en estado activo: se cuenta desde que se creo
    public boolean superoVigencia(LocalDateTime ahora) {
        return ahora.isAfter(fechaCreacion.plusDays(vigenciaDias));
    }

    public void sumarAcceso(LocalDateTime ahora) {
        this.cantidadAccesos = cantidadAccesos + 1;
        this.ultimoIngreso = ahora;
    }
}
