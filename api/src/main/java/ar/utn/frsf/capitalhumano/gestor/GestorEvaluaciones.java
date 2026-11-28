package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.configuracion.RepositorioConfiguracion;
import ar.utn.frsf.capitalhumano.dao.*;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.excepcion.CompetenciasNoEvaluablesException;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import ar.utn.frsf.capitalhumano.exportacion.ExportadorClaves;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

/**
 * CU025 Evaluar candidatos: busqueda y control de candidatos, validacion del puesto, claves,
 * registro de la evaluacion con la copia del modelo y los cuestionarios, y exportacion de claves.
 */
@Service
public class GestorEvaluaciones {

    static final String SIN_FILTRO = "Debe completar al menos un filtro";
    static final String NO_EVALUABLES = "Las siguientes competencias del puesto no pueden ser evaluadas:";
    private static final String CARACTERES_CLAVE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LARGO_CLAVE = 8;
    private static final int MINIMO_PREGUNTAS = 2;

    private final CandidatoDAO candidatoDAO;
    private final CuestionarioDAO cuestionarioDAO;
    private final PuestoDAO puestoDAO;
    private final EvaluacionDAO evaluacionDAO;
    private final ConsultorDAO consultorDAO;
    private final RepositorioConfiguracion configuracion;
    private final ExportadorClaves exportador;
    private final Supplier<LocalDateTime> reloj;
    private final IntUnaryOperator azar;

    @Autowired
    public GestorEvaluaciones(CandidatoDAO candidatoDAO, CuestionarioDAO cuestionarioDAO, PuestoDAO puestoDAO,
                              EvaluacionDAO evaluacionDAO, ConsultorDAO consultorDAO, RepositorioConfiguracion configuracion,
                              ExportadorClaves exportador) {
        this(candidatoDAO, cuestionarioDAO, puestoDAO, evaluacionDAO, consultorDAO, configuracion, exportador,
                LocalDateTime::now, new SecureRandom()::nextInt);
    }

    // para los tests: hora fija y claves repetibles
    GestorEvaluaciones(CandidatoDAO candidatoDAO, CuestionarioDAO cuestionarioDAO, PuestoDAO puestoDAO,
                       EvaluacionDAO evaluacionDAO, ConsultorDAO consultorDAO, RepositorioConfiguracion configuracion,
                       ExportadorClaves exportador, Supplier<LocalDateTime> reloj, IntUnaryOperator azar) {
        this.candidatoDAO = candidatoDAO;
        this.cuestionarioDAO = cuestionarioDAO;
        this.puestoDAO = puestoDAO;
        this.evaluacionDAO = evaluacionDAO;
        this.consultorDAO = consultorDAO;
        this.configuracion = configuracion;
        this.exportador = exportador;
        this.reloj = reloj;
        this.azar = azar;
    }

    /** Paso 3: candidatos sin baja por apellido, nombre ("contiene") y/o numero. Pide al menos un filtro. */
    @Transactional(readOnly = true)
    public List<CandidatoGestionDTO> buscarCandidatos(FiltroCandidatoDTO f) {
        boolean hayFiltro = f != null && (texto(f.getApellido()) || texto(f.getNombre()) || f.getNroCandidato() != null);
        if (!hayFiltro) {
            throw new ValidacionException(SIN_FILTRO);
        }
        List<Candidato> candidatos = candidatoDAO.buscarPorFiltros(f.getApellido(), f.getNombre(), f.getNroCandidato());
        List<CandidatoGestionDTO> resultado = new ArrayList<>();
        for (Candidato c : candidatos) {
            resultado.add(gestionDTO(c));
        }
        return resultado;
    }

    /** Paso 6: los candidatos que ya tienen un cuestionario Activo o En proceso (no se pueden evaluar). */
    @Transactional(readOnly = true)
    public List<CandidatoGestionDTO> verificarCuestionariosActivos(List<Long> ids) {
        List<CandidatoGestionDTO> conActivos = new ArrayList<>();
        for (Long idCandidato : ids == null ? List.<Long>of() : ids) {
            Optional<Cuestionario> activo = cuestionarioDAO.buscarActivoOEnProceso(idCandidato);
            activo.ifPresent(c -> conActivos.add(gestionDTO(c.getCandidato())));
        }
        return conActivos;
    }

    /**
     * Pasos 8 y 9: cada competencia del puesto tiene que tener al menos un factor con 2 o mas
     * preguntas activas; si no, 9.A con la lista de las que no se pueden evaluar.
     */
    @Transactional(readOnly = true)
    public List<CompetenciaRequeridaDTO> validarPuesto(Long idPuesto) {
        Puesto p = buscarPuesto(idPuesto);
        List<CompetenciaRequerida> requeridas = p.getCompetenciasRequeridas();
        List<String> noEvaluables = new ArrayList<>();
        for (CompetenciaRequerida cr : requeridas) {
            int evaluables = 0;
            for (Factor f : cr.getCompetencia().getFactores()) {
                if (f.getFechaBaja() == null && f.getPreguntasActivas().size() >= MINIMO_PREGUNTAS) {
                    evaluables++;
                }
            }
            if (evaluables == 0) {
                noEvaluables.add(cr.getCompetencia().getNombre());
            }
        }
        if (!noEvaluables.isEmpty()) {
            throw new CompetenciasNoEvaluablesException(NO_EVALUABLES, noEvaluables);
        }
        return requeridas.stream()
                .map(cr -> new CompetenciaRequeridaDTO(cr.getCompetencia().getId(), cr.getCompetencia().getNombre(), cr.getPonderacion()))
                .toList();
    }

    /** Paso 10: una clave nueva de 8 letras y digitos para cada candidato. */
    @Transactional(readOnly = true)
    public List<CandidatoClaveDTO> generarClaves(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ValidacionException("Se debe seleccionar uno o más candidatos");
        }
        List<CandidatoClaveDTO> claves = new ArrayList<>();
        for (Long idCandidato : ids) {
            Candidato candidato = buscarCandidato(idCandidato);
            String clave = generarClave();
            claves.add(claveDTO(candidato, clave));
        }
        return claves;
    }

    /**
     * Paso 12: crea la evaluacion con la copia del modelo del puesto ("mismo modelo") y un
     * cuestionario Activo por candidato con las mismas claves que se mostraron. Todo en una transaccion.
     */
    @Transactional
    public EvaluacionDTO registrarEvaluacion(NuevaEvaluacionDTO datos, String username) {
        validar(datos);
        LocalDateTime ahora = reloj.get();
        Puesto p = buscarPuesto(datos.getIdPuesto());
        Consultor consultor = consultorDAO.buscarPorUsername(username)
                .orElseThrow(() -> new NoSuchElementException("El consultor " + username + " no existe"));
        Evaluacion ev = new Evaluacion(ahora, p, consultor);
        copiarModelo(p).forEach(ev::agregarCompetenciaEvaluada);

        Integer porBloque = configuracion.getPreguntasPorBloque();
        Integer tiempoMaximo = configuracion.getTiempoMaximoMinutos();
        Integer vigencia = configuracion.getVigenciaDias();
        Integer maxAccesos = configuracion.getMaxAccesos();
        for (CandidatoClaveDTO cc : datos.getCandidatos()) {
            Candidato candidato = buscarCandidato(cc.getIdCandidato());
            Cuestionario c = new Cuestionario(candidato, cc.getClave().toUpperCase(Locale.ROOT), porBloque, tiempoMaximo,
                    vigencia, maxAccesos, ahora);
            ev.agregarCuestionario(c);
        }
        evaluacionDAO.guardar(ev);
        return evaluacionDTO(ev);
    }

    /** Pasos 14 a 17: el Excel con los candidatos de la evaluacion y sus claves. */
    @Transactional(readOnly = true)
    public ArchivoDTO exportarClaves(Long idEvaluacion) {
        Evaluacion ev = evaluacionDAO.buscarPorId(idEvaluacion)
                .orElseThrow(() -> new NoSuchElementException("La evaluación no existe"));
        List<CandidatoClaveDTO> claves = new ArrayList<>();
        for (Cuestionario c : ev.getCuestionarios()) {
            claves.add(claveDTO(c.getCandidato(), c.getClave()));
        }
        return exportador.exportar(ev.getPuesto().getNombre(), ev.getFechaCreacion(), claves);
    }

    /**
     * Copia de las competencias del puesto con su ponderacion minima, sus factores con 2 o mas
     * preguntas activas y esas preguntas con sus opciones y ponderaciones. El cuestionario
     * se arma sobre esta copia, asi los cambios posteriores al modelo no lo afectan.
     */
    private List<CompetenciaEvaluada> copiarModelo(Puesto p) {
        List<CompetenciaEvaluada> copia = new ArrayList<>();
        for (CompetenciaRequerida cr : p.getCompetenciasRequeridas()) {
            CompetenciaEvaluada ce = new CompetenciaEvaluada(cr.getCompetencia(), cr.getPonderacion());
            for (Factor f : cr.getCompetencia().getFactores()) {
                List<Pregunta> activas = f.getPreguntasActivas();
                if (f.getFechaBaja() != null || activas.size() < MINIMO_PREGUNTAS) {
                    continue;
                }
                FactorEvaluado fe = new FactorEvaluado(f);
                for (Pregunta pr : activas) {
                    fe.agregarPregunta(new PreguntaEvaluada(pr));
                }
                ce.agregarFactor(fe);
            }
            copia.add(ce);
        }
        return copia;
    }

    private String generarClave() {
        StringBuilder clave = new StringBuilder(LARGO_CLAVE);
        for (int i = 0; i < LARGO_CLAVE; i++) {
            clave.append(CARACTERES_CLAVE.charAt(azar.applyAsInt(CARACTERES_CLAVE.length())));
        }
        return clave.toString();
    }

    // datos minimos para registrar: puesto, al menos un candidato, sin repetidos y con clave de 8
    private static void validar(NuevaEvaluacionDTO datos) {
        List<String> errores = new ArrayList<>();
        if (datos == null || datos.getIdPuesto() == null) errores.add("Se debe seleccionar una función");
        List<CandidatoClaveDTO> candidatos = datos == null || datos.getCandidatos() == null ? List.of() : datos.getCandidatos();
        if (candidatos.isEmpty()) errores.add("Se debe seleccionar uno o más candidatos");
        if (candidatos.stream().map(CandidatoClaveDTO::getIdCandidato).distinct().count() != candidatos.size()) {
            errores.add("Hay candidatos repetidos en la evaluación");
        }
        if (candidatos.stream().anyMatch(c -> c.getClave() == null || !c.getClave().matches("[A-Za-z0-9]{8}"))) {
            errores.add("Cada clave de ingreso debe tener 8 letras o dígitos");
        }
        if (!errores.isEmpty()) {
            throw new ValidacionException("No se pudo registrar la evaluación. Corrija lo siguiente:", errores);
        }
    }

    private Puesto buscarPuesto(Long idPuesto) {
        return puestoDAO.buscarPorId(idPuesto)
                .filter(p -> p.getFechaBaja() == null)
                .orElseThrow(() -> new NoSuchElementException("El puesto no existe"));
    }

    private Candidato buscarCandidato(Long idCandidato) {
        return candidatoDAO.buscarPorId(idCandidato)
                .orElseThrow(() -> new NoSuchElementException("El candidato " + idCandidato + " no existe"));
    }

    private static boolean texto(String s) {
        return s != null && !s.isBlank();
    }

    private static CandidatoGestionDTO gestionDTO(Candidato c) {
        return new CandidatoGestionDTO(c.getId(), c.getNroCandidato(), c.getApellido(), c.getNombre(), c.getTipoDocumento(),
                c.getNroDocumento());
    }

    private static CandidatoClaveDTO claveDTO(Candidato c, String clave) {
        return new CandidatoClaveDTO(c.getId(), c.getApellido(), c.getNombre(), c.getTipoDocumento(), c.getNroDocumento(), clave);
    }

    static EvaluacionDTO evaluacionDTO(Evaluacion ev) {
        return new EvaluacionDTO(ev.getId(), ev.getFechaCreacion(), ev.getPuesto().getNombre(),
                ev.getPuesto().getEmpresa().getNombre(), ev.getCuestionarios().size());
    }
}
