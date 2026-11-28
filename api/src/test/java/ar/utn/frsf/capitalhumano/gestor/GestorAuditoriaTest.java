package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.RegistroAuditoriaDAO;
import ar.utn.frsf.capitalhumano.dominio.Consultor;
import ar.utn.frsf.capitalhumano.dominio.RegistroAuditoria;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GestorAuditoriaTest {

    @Test
    void registrarBajaGuardaQuienCuandoYQue() {
        RegistroAuditoriaDAO dao = mock(RegistroAuditoriaDAO.class);
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 27, 11, 0);
        Consultor jperez = new Consultor("JPEREZ");
        when(dao.guardar(any())).thenReturn(true);

        assertTrue(new GestorAuditoria(dao, () -> ahora).registrarBaja("BAJA", "PREGUNTA", 5L, jperez));

        ArgumentCaptor<RegistroAuditoria> r = ArgumentCaptor.forClass(RegistroAuditoria.class);
        verify(dao).guardar(r.capture());
        assertEquals(ahora, r.getValue().getFechaHora());
        assertEquals("BAJA", r.getValue().getAccion());
        assertEquals("PREGUNTA", r.getValue().getEntidadAfectada());
        assertEquals(5L, r.getValue().getIdObjetoAfectado());
        assertSame(jperez, r.getValue().getConsultor());
    }
}
