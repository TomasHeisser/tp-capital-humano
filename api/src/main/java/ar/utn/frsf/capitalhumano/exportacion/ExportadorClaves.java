package ar.utn.frsf.capitalhumano.exportacion;

import ar.utn.frsf.capitalhumano.dto.ArchivoDTO;
import ar.utn.frsf.capitalhumano.dto.CandidatoClaveDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Exporta la lista de candidatos de una evaluacion con sus claves de ingreso.
 */
public interface ExportadorClaves {

    ArchivoDTO exportar(String puesto, LocalDateTime fecha, List<CandidatoClaveDTO> claves);
}
