package ar.utn.frsf.capitalhumano.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Archivo generado (nombre + bytes) para que el navegador lo descargue.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoDTO {

    private String nombre;
    private byte[] contenido;
}
