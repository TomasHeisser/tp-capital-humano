package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.RegistroAuditoriaDAO;
import ar.utn.frsf.capitalhumano.dominio.Consultor;
import ar.utn.frsf.capitalhumano.dominio.RegistroAuditoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Deja registrado quien dio de baja algo y cuando. Lo llaman los gestores que hacen bajas,
 * dentro de su misma transaccion (si falla la auditoria, no hay baja).
 */
@Service
public class GestorAuditoria {

    private final RegistroAuditoriaDAO registroAuditoriaDAO;
    private final Supplier<LocalDateTime> reloj;

    @Autowired
    public GestorAuditoria(RegistroAuditoriaDAO registroAuditoriaDAO) {
        this(registroAuditoriaDAO, LocalDateTime::now);
    }

    // para los tests: hora fija
    GestorAuditoria(RegistroAuditoriaDAO registroAuditoriaDAO, Supplier<LocalDateTime> reloj) {
        this.registroAuditoriaDAO = registroAuditoriaDAO;
        this.reloj = reloj;
    }

    /** Crea el registro con la fecha y hora actual y lo guarda. */
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean registrarBaja(String accion, String entidad, Long idObjeto, Consultor c) {
        RegistroAuditoria r = new RegistroAuditoria(reloj.get(), accion, entidad, idObjeto, c);
        return registroAuditoriaDAO.guardar(r);
    }
}
