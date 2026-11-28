package ar.utn.frsf.capitalhumano.dominio;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Puesto o funcion que pide una empresa, con las competencias que requiere.
 */
@Entity
@Table(name = "puesto")
@Getter
@Setter
@NoArgsConstructor
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompetenciaRequerida> competenciasRequeridas = new ArrayList<>();

    public Puesto(String codigo, String nombre, String descripcion, Empresa empresa) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.empresa = empresa;
    }

    // cada competencia va una sola vez por puesto (eso lo controla el gestor antes)
    public void agregarCompetencia(Competencia competencia, BigDecimal ponderacion) {
        competenciasRequeridas.add(new CompetenciaRequerida(this, competencia, ponderacion));
    }
}
