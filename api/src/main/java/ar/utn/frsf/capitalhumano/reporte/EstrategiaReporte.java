package ar.utn.frsf.capitalhumano.reporte;

import ar.utn.frsf.capitalhumano.dto.CriterioReporteDTO;
import ar.utn.frsf.capitalhumano.dto.ReporteDTO;

/**
 * Estrategia de un reporte (patron Strategy): GestorReportes elige cual usar segun el TipoReporte.
 */
public interface EstrategiaReporte {

    ReporteDTO generar(CriterioReporteDTO criterio);
}
