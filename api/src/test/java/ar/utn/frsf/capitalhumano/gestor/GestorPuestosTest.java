package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.utn.frsf.capitalhumano.dominio.Empresa;
import ar.utn.frsf.capitalhumano.dominio.Puesto;
import ar.utn.frsf.capitalhumano.dto.FiltroPuestoDTO;
import ar.utn.frsf.capitalhumano.dto.PuestoDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorPuestosTest {

    @Mock
    private PuestoDAO puestoDAO;
    @InjectMocks
    private GestorPuestos gestor;

    @Test
    void buscaYDevuelveLasFilasDeLaGrilla() {
        Puesto p = new Puesto("DEV-JR-01", "DESARROLLADOR JAVA JUNIOR", null, new Empresa("TECNOSUR S.A."));
        p.setId(1L);
        when(puestoDAO.buscarPorFiltros(null, "java", null)).thenReturn(List.of(p));

        List<PuestoDTO> r = gestor.buscarPuestos(new FiltroPuestoDTO(null, "java", null));

        assertEquals(List.of(new PuestoDTO(1L, "DEV-JR-01", "DESARROLLADOR JAVA JUNIOR", "TECNOSUR S.A.")), r);
    }

    @Test
    void sinCriterioNoBusca() {
        var e = assertThrows(ValidacionException.class, () -> gestor.buscarPuestos(new FiltroPuestoDTO(" ", null, "")));
        assertEquals("Debe ingresar al menos un criterio de búsqueda", e.getMessage());
        assertThrows(ValidacionException.class, () -> gestor.buscarPuestos(null));
        verifyNoInteractions(puestoDAO);
    }
}
