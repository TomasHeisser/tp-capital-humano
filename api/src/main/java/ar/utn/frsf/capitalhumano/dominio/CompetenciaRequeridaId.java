package ar.utn.frsf.capitalhumano.dominio;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// clave compuesta de la tabla requiere (puesto + competencia)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CompetenciaRequeridaId implements Serializable {
    private Long puesto;
    private Long competencia;
}
