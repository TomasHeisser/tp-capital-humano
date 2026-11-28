package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lo que se elige en la ventana de "Generar con IA".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudIADTO {

    private Long idCompetencia;
    private Long idFactor;
}
