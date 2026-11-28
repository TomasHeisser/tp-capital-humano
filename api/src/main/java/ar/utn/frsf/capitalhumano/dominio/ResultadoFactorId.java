package ar.utn.frsf.capitalhumano.dominio;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// clave compuesta de resultado_factor (cuestionario + factor evaluado)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ResultadoFactorId implements Serializable {
    private Long idCuestionario;
    private Long idFactorEvaluado;
}
