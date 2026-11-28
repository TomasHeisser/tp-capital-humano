package ar.utn.frsf.capitalhumano.dominio;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

// clave compuesta de bloque_cuestionario (cuestionario + nro de bloque)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BloqueCuestionarioId implements Serializable {
    private Long idCuestionario;
    private Integer nroBloque;
}
