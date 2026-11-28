package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.CompetenciaDAO;
import ar.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.utn.frsf.capitalhumano.dominio.Competencia;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import ar.utn.frsf.capitalhumano.dto.CompetenciaDTO;
import ar.utn.frsf.capitalhumano.dto.FactorDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de competencias y factores. Por ahora los listados que cargan los combos
 * de preguntas (CU020 y CU021).
 */
@Service
public class GestorCompetencias {

    private final CompetenciaDAO competenciaDAO;
    private final FactorDAO factorDAO;

    public GestorCompetencias(CompetenciaDAO competenciaDAO, FactorDAO factorDAO) {
        this.competenciaDAO = competenciaDAO;
        this.factorDAO = factorDAO;
    }

    /** Todas las competencias activas, por nombre. */
    @Transactional(readOnly = true)
    public List<CompetenciaDTO> listarCompetencias() {
        return competenciaDAO.buscarTodos().stream().map(GestorCompetencias::aDTO).toList();
    }

    /** Factores activos de una competencia, por nro de orden. */
    @Transactional(readOnly = true)
    public List<FactorDTO> listarFactores(Long idCompetencia) {
        return factorDAO.buscarPorCompetencia(idCompetencia).stream().map(GestorCompetencias::aDTO).toList();
    }

    static CompetenciaDTO aDTO(Competencia c) {
        return new CompetenciaDTO(c.getId(), c.getCodigo(), c.getNombre(), c.getDescripcion());
    }

    static FactorDTO aDTO(Factor f) {
        return new FactorDTO(f.getId(), f.getCodigo(), f.getNombre(), f.getDescripcion(), f.getNroOrden(),
                f.getCompetencia().getId(), f.getCompetencia().getNombre());
    }
}
