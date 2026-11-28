package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.CompetenciaDAO;
import ar.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.utn.frsf.capitalhumano.dominio.Competencia;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import ar.utn.frsf.capitalhumano.dto.CompetenciaDTO;
import ar.utn.frsf.capitalhumano.dto.FactorDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestorCompetenciasTest {

    @Mock
    private CompetenciaDAO competenciaDAO;
    @Mock
    private FactorDAO factorDAO;
    @InjectMocks
    private GestorCompetencias gestor;

    @Test
    void listaCompetenciasYFactoresComoDTO() {
        Competencia c = new Competencia("COMP-GP", "GESTIÓN DE PROYECTOS", "DESC");
        c.setId(1L);
        Factor f = new Factor("FAC-GP-ALC", "GESTIÓN DEL ALCANCE", null, 1, c);
        f.setId(2L);
        when(competenciaDAO.buscarTodos()).thenReturn(List.of(c));
        when(factorDAO.buscarPorCompetencia(1L)).thenReturn(List.of(f));

        assertEquals(List.of(new CompetenciaDTO(1L, "COMP-GP", "GESTIÓN DE PROYECTOS", "DESC")), gestor.listarCompetencias());
        assertEquals(List.of(new FactorDTO(2L, "FAC-GP-ALC", "GESTIÓN DEL ALCANCE", null, 1, 1L, "GESTIÓN DE PROYECTOS")),
                gestor.listarFactores(1L));
    }
}
