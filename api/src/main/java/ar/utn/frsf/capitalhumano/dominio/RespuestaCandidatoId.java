package ar.utn.frsf.capitalhumano.dominio;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// clave compuesta de responde (bloque + pregunta evaluada)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RespuestaCandidatoId implements Serializable {
    private Long idCuestionario;
    private Integer nroBloque;
    private Long idPreguntaEvaluada;
}
