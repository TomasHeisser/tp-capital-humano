package ar.utn.frsf.capitalhumano.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Orden de merito: los que alcanzaron el minimo, los que no y el resto agrupado por estado.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OrdenMeritoDTO extends ReporteDTO {

    private List<FilaOrdenMeritoDTO> ordenMerito;
    private List<FilaOrdenMeritoDTO> noAlcanzaronMinimo;
    private Map<String, List<FilaOrdenMeritoDTO>> restoPorEstado;

    public OrdenMeritoDTO(String codigoPuesto, String nombrePuesto, String empresa, List<FilaOrdenMeritoDTO> ordenMerito,
                          List<FilaOrdenMeritoDTO> noAlcanzaronMinimo, Map<String, List<FilaOrdenMeritoDTO>> restoPorEstado,
                          LocalDateTime fechaEmision) {
        super(codigoPuesto, nombrePuesto, empresa, fechaEmision);
        this.ordenMerito = ordenMerito;
        this.noAlcanzaronMinimo = noAlcanzaronMinimo;
        this.restoPorEstado = restoPorEstado;
    }
}
