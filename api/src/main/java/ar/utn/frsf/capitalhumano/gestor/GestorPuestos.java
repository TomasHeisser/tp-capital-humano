package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.PuestoDAO;
import ar.utn.frsf.capitalhumano.dominio.Puesto;
import ar.utn.frsf.capitalhumano.dto.FiltroPuestoDTO;
import ar.utn.frsf.capitalhumano.dto.PuestoDTO;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

/**
 * Logica de los puestos. Por ahora la busqueda, que la usa tambien Evaluar candidatos (CU025).
 */
@Service
public class GestorPuestos {

    static final String SIN_CRITERIO = "Debe ingresar al menos un criterio de búsqueda";

    private final PuestoDAO puestoDAO;

    public GestorPuestos(PuestoDAO puestoDAO) {
        this.puestoDAO = puestoDAO;
    }

    /**
     * Busca puestos activos por codigo, nombre y/o empresa ("contiene"). Pide al menos un criterio.
     */
    @Transactional(readOnly = true)
    public List<PuestoDTO> buscarPuestos(FiltroPuestoDTO filtro) {
        boolean hayCriterio = filtro != null && Stream.of(filtro.getCodigo(), filtro.getNombre(), filtro.getEmpresa())
                .anyMatch(s -> s != null && !s.isBlank());
        if (!hayCriterio) {
            throw new ValidacionException(SIN_CRITERIO);
        }
        return puestoDAO.buscarPorFiltros(filtro.getCodigo(), filtro.getNombre(), filtro.getEmpresa()).stream()
                .map(GestorPuestos::aDTO)
                .toList();
    }

    private static PuestoDTO aDTO(Puesto p) {
        return new PuestoDTO(p.getId(), p.getCodigo(), p.getNombre(), p.getEmpresa().getNombre());
    }
}
