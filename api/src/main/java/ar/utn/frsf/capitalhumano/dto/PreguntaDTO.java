package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Datos completos de una pregunta (alta, modificacion y lo que devuelve la IA).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreguntaDTO {

    private Long id;
    private Long idCompetencia;
    private Long idFactor;
    private String nombre;
    private String enunciado;
    private String descripcion;
    private Long idOpcionRespuesta;
    private List<PonderacionDTO> ponderaciones = new ArrayList<>();
}
