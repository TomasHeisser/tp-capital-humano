package ar.utn.frsf.capitalhumano.dominio;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// clave compuesta de obtiene_resultado (cuestionario + competencia evaluada)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ResultadoId implements Serializable {
    private Long idCuestionario;
    private Long idCompetenciaEvaluada;
}
