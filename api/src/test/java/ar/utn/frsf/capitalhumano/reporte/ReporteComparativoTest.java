package ar.utn.frsf.capitalhumano.reporte;

import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dominio.Cuestionario;
import ar.utn.frsf.capitalhumano.dominio.EstadoCuestionario;
import ar.utn.frsf.capitalhumano.dominio.TipoDocumento;
import ar.utn.frsf.capitalhumano.dto.CriterioReporteDTO;
import ar.utn.frsf.capitalhumano.dto.FilaComparativoDTO;
import ar.utn.frsf.capitalhumano.dto.ReporteComparativoDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReporteComparativoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 27, 11, 20);

    @Test
    void comparaRequeridoContraObtenidoEnCadaCompetencia() {
        CuestionarioDAO dao = mock(CuestionarioDAO.class);
        Datos d = new Datos();
        Cuestionario gomez = d.completo(Datos.candidato("GÓMEZ", "MARÍA", TipoDocumento.DNI, "30123456"),
                LocalDateTime.of(2026, 11, 20, 18, 2), 38, 1, "8.50", "7.75", "8.50");
        Collections.reverse(gomez.getResultados()); // aunque vengan desordenados salen en el orden de la evaluacion
        when(dao.buscarPorId(gomez.getId())).thenReturn(Optional.of(gomez));

        ReporteComparativoDTO r = (ReporteComparativoDTO) new ReporteComparativo(dao, () -> AHORA)
                .generar(new CriterioReporteDTO(1L, null, gomez.getId()));

        assertEquals("GÓMEZ, MARÍA", r.getCandidato());
        assertEquals("DESARROLLADOR JAVA JUNIOR", r.getNombrePuesto());
        assertEquals("DEV-JR-01", r.getCodigoPuesto());
        assertEquals(AHORA, r.getFechaEmision());
        assertEquals(List.of("PROGRAMACIÓN JAVA", "GESTIÓN DE PROYECTOS", "LEALTAD"),
                r.getFilas().stream().map(FilaComparativoDTO::getCompetencia).toList());
        assertEquals(new BigDecimal("6"), r.getFilas().getFirst().getRequerido());
        assertEquals(new BigDecimal("8.50"), r.getFilas().getFirst().getObtenido());
    }

    @Test
    void sinCandidatoOConCuestionarioNoCompleto() {
        CuestionarioDAO dao = mock(CuestionarioDAO.class);
        Datos d = new Datos();
        Cuestionario activo = d.cuestionario(Datos.candidato("LÓPEZ", "CAMILA", TipoDocumento.DNI, "1"), EstadoCuestionario.ACTIVO);
        when(dao.buscarPorId(activo.getId())).thenReturn(Optional.of(activo));
        ReporteComparativo reporte = new ReporteComparativo(dao);

        ValidacionException e = assertThrows(ValidacionException.class, () -> reporte.generar(new CriterioReporteDTO(1L, null, null)));
        assertEquals("Se debe seleccionar al menos un candidato", e.getMessage());
        assertThrows(ValidacionException.class, () -> reporte.generar(null));
        assertThrows(NoSuchElementException.class, () -> reporte.generar(new CriterioReporteDTO(1L, null, activo.getId())));
    }
}
