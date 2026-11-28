package ar.utn.frsf.capitalhumano.dominio;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// clave compuesta de la tabla pondera (pregunta + item)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PonderacionPreguntaOpcionId implements Serializable {
    private Long pregunta;
    private Long item;
}
