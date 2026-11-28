package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.configuracion.RepositorioConfiguracion;
import ar.utn.frsf.capitalhumano.dao.CuestionarioDAO;
import ar.utn.frsf.capitalhumano.dao.RegistroEjecucionDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.excepcion.RespuestasIncompletasException;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * CU026 Completar cuestionario y CU032 Finalizar cuestionarios vencidos.
 * Lo que se puede hacer en cada momento lo decide el estado del cuestionario (patron State);
 * aca se coordina: se busca, se le pide al cuestionario y se guarda.
 */
@Service
public class GestorCuestionario {

    static final String M31 = "Se superó la cantidad máxima de accesos permitidos. El cuestionario pasa a estado INCOMPLETO.";
    static final String M32 = "Se superó el tiempo máximo para completar el cuestionario. El cuestionario pasa a estado INCOMPLETO.";
    static final String M33 = "El cuestionario superó el tiempo máximo permitido en estado activo y pasa a estado SIN CONTESTAR.";
    static final String M34 = "No se puede continuar. Faltan responder las siguientes preguntas:";
    static final String M35 = "El Cuestionario ha sido completado con éxito";
    static final String NO_DISPONIBLE = "El cuestionario ya no se puede completar.";

    private static final int PREGUNTAS_POR_FACTOR = 2;
    private static final BigDecimal DOS = BigDecimal.valueOf(2);

    private final CuestionarioDAO cuestionarioDAO;
    private final RegistroEjecucionDAO registroEjecucionDAO;
    private final RepositorioConfiguracion configuracion;
    private final Supplier<LocalDateTime> reloj;
    private final Random azar;

    @Autowired
    public GestorCuestionario(CuestionarioDAO cuestionarioDAO, RegistroEjecucionDAO registroEjecucionDAO,
                              RepositorioConfiguracion configuracion) {
        this(cuestionarioDAO, registroEjecucionDAO, configuracion, LocalDateTime::now, new Random());
    }

    // para los tests: hora fija y sorteo repetible
    GestorCuestionario(CuestionarioDAO cuestionarioDAO, RegistroEjecucionDAO registroEjecucionDAO,
                       RepositorioConfiguracion configuracion, Supplier<LocalDateTime> reloj, Random azar) {
        this.cuestionarioDAO = cuestionarioDAO;
        this.registroEjecucionDAO = registroEjecucionDAO;
        this.configuracion = configuracion;
        this.reloj = reloj;
        this.azar = azar;
    }

    /**
     * Ingreso del candidato (paso 1 y alternativos 1.A, 1.B, 1.C y precondicion).
     * Siempre cuenta el acceso; si vencio informa el motivo, si es el primer ingreso manda las
     * instrucciones y si ya habia empezado devuelve el bloque pendiente.
     */
    @Transactional
    public CuestionarioDTO ingresar(Long idCuestionario) {
        Cuestionario c = buscar(idCuestionario);
        LocalDateTime ahora = reloj.get();
        boolean puedeContinuar = c.registrarAcceso(ahora);
        cuestionarioDAO.actualizar(c);

        String estado = c.getEstado().getNombre();
        if (!puedeContinuar) {
            return dto(c, motivoDeCierre(c), null, null);
        }
        if (EstadoCuestionario.EN_PROCESO.equals(estado)) {
            // 1.C: ya habia empezado, va directo al bloque que falta
            BloqueCuestionario pendiente = c.getBloquePendiente();
            String aviso = "Bienvenida nuevamente. Se continúa en el bloque " + pendiente.getNroBloque() + ", que aún no fue respondido.";
            return dto(c, aviso, null, crearBloqueDTO(c, pendiente));
        }
        return dto(c, null, armarInstrucciones(c), null);
    }

    /**
     * Acepto las instrucciones (paso 3): sortea las preguntas, arma los bloques y pasa a En proceso.
     */
    @Transactional
    public BloqueDTO comenzar(Long idCuestionario) {
        Cuestionario c = buscar(idCuestionario);
        List<PreguntaEvaluada> preguntas = sortearPreguntas(c.getEvaluacion());
        List<BloqueCuestionario> bloques = armarBloques(preguntas, c.getPreguntasPorBloque());
        c.iniciar(bloques, reloj.get());
        cuestionarioDAO.actualizar(c);
        return crearBloqueDTO(c, bloques.getFirst());
    }

    /**
     * Guarda las respuestas de un bloque (pasos 5 a 8). Si era el ultimo calcula los resultados
     * y completa el cuestionario; si no, devuelve el siguiente bloque.
     */
    @Transactional
    public CuestionarioDTO responderBloque(Long idCuestionario, Integer nroBloque, List<RespuestaDTO> r) {
        Cuestionario c = buscar(idCuestionario);
        if (!EstadoCuestionario.EN_PROCESO.equals(c.getEstado().getNombre())) {
            throw new IllegalStateException(NO_DISPONIBLE);
        }
        BloqueCuestionario b = c.getBloquePendiente();
        // un bloque respondido no se puede volver a mandar
        if (b == null || !b.getNroBloque().equals(nroBloque)) {
            throw new ValidacionException("El bloque " + nroBloque + " no es el que está pendiente de responder");
        }
        List<RespuestaDTO> respuestas = r == null ? List.of() : r;
        validarRespuestas(c, b, respuestas);

        LocalDateTime ahora = reloj.get();
        for (RespuestaDTO dto : respuestas) {
            PreguntaEvaluada p = b.getPreguntas().stream()
                    .filter(x -> x.getId().equals(dto.getIdPregunta()))
                    .findFirst()
                    .orElseThrow(() -> new ValidacionException("La pregunta " + dto.getIdPregunta() + " no es de este bloque"));
            b.registrarRespuesta(p, p.getOpcion(dto.getIdOpcion()), ahora);
        }

        BloqueCuestionario siguiente = c.getBloquePendiente();
        if (siguiente != null) {
            // 8.A: quedan bloques
            cuestionarioDAO.actualizar(c);
            return dto(c, null, null, crearBloqueDTO(c, siguiente));
        }
        // paso 8: era el ultimo
        List<Resultado> resultados = calcularResultados(c);
        c.completar(resultados, ahora);
        cuestionarioDAO.actualizar(c);
        return dto(c, M35, null, null);
    }

    /**
     * CU032: pasa a Incompleto o Sin contestar los cuestionarios vencidos y deja registro de inicio y fin.
     *
     * @return cuantos cuestionarios cambiaron de estado
     */
    @Transactional
    public int finalizarVencidos() {
        registroEjecucionDAO.guardar(new RegistroEjecucion(reloj.get(), AccionEjecucion.INICIO));
        LocalDateTime ahora = reloj.get();
        int cambiados = 0;
        for (Cuestionario c : cuestionarioDAO.buscarPendientes()) {
            if (c.verificarVencimiento(ahora)) {
                cuestionarioDAO.actualizar(c);
                cambiados++;
            }
        }
        registroEjecucionDAO.guardar(new RegistroEjecucion(reloj.get(), AccionEjecucion.FIN));
        return cambiados;
    }

    // ---------------------------------------------------------------- privados

    /** 2 preguntas al azar de cada factor evaluado, y despues todas mezcladas. */
    private List<PreguntaEvaluada> sortearPreguntas(Evaluacion e) {
        List<PreguntaEvaluada> elegidas = new ArrayList<>();
        for (CompetenciaEvaluada ce : e.getCompetenciasEvaluadas()) {
            for (FactorEvaluado fe : ce.getFactores()) {
                List<PreguntaEvaluada> delFactor = new ArrayList<>(fe.getPreguntas());
                Collections.shuffle(delFactor, azar);
                elegidas.addAll(delFactor.subList(0, Math.min(PREGUNTAS_POR_FACTOR, delFactor.size())));
            }
        }
        Collections.shuffle(elegidas, azar);
        return elegidas;
    }

    /** Corta la lista en bloques de tam preguntas (el ultimo puede quedar mas corto). */
    private List<BloqueCuestionario> armarBloques(List<PreguntaEvaluada> preguntas, Integer tam) {
        int cantidad = (preguntas.size() + tam - 1) / tam;
        return IntStream.range(0, cantidad)
                .mapToObj(i -> new BloqueCuestionario(i + 1,
                        preguntas.subList(i * tam, Math.min((i + 1) * tam, preguntas.size()))))
                .toList();
    }

    /**
     * Puntajes (observacion 3 del CU): factor = suma de lo elegido en sus preguntas / 2;
     * competencia = promedio de sus factores. Se redondea a 2 decimales.
     */
    private List<Resultado> calcularResultados(Cuestionario c) {
        Map<Long, BigDecimal> elegidoPorPregunta = new HashMap<>();
        c.getBloques().forEach(b -> b.getRespuestas().forEach(r ->
                elegidoPorPregunta.put(r.getPreguntaEvaluada().getId(), r.getOpcionEvaluada().getPonderacion())));

        List<Resultado> resultados = new ArrayList<>();
        for (CompetenciaEvaluada ce : c.getEvaluacion().getCompetenciasEvaluadas()) {
            List<ResultadoFactor> porFactor = new ArrayList<>();
            for (FactorEvaluado fe : ce.getFactores()) {
                BigDecimal suma = fe.getPreguntas().stream()
                        .map(p -> elegidoPorPregunta.getOrDefault(p.getId(), BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                porFactor.add(new ResultadoFactor(fe, suma.divide(DOS, 2, RoundingMode.HALF_UP)));
            }
            BigDecimal total = porFactor.stream().map(ResultadoFactor::getPuntaje).reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(porFactor.size()), 2, RoundingMode.HALF_UP);
            resultados.add(new Resultado(ce, total, porFactor));
        }
        return resultados;
    }

    // 6.A: tiene que haber una respuesta para cada pregunta del bloque (se informan con su numero en el cuestionario)
    private void validarRespuestas(Cuestionario c, BloqueCuestionario b, List<RespuestaDTO> respuestas) {
        Set<Long> respondidas = new HashSet<>();
        respuestas.stream()
                .filter(x -> x.getIdPregunta() != null && x.getIdOpcion() != null)
                .forEach(x -> respondidas.add(x.getIdPregunta()));
        int primerNumero = (b.getNroBloque() - 1) * c.getPreguntasPorBloque();
        List<String> faltan = new ArrayList<>();
        for (int i = 0; i < b.getPreguntas().size(); i++) {
            if (!respondidas.contains(b.getPreguntas().get(i).getId())) {
                faltan.add("Pregunta " + (primerNumero + i + 1) + ".");
            }
        }
        if (!faltan.isEmpty()) {
            throw new RespuestasIncompletasException(M34, faltan);
        }
    }

    private BloqueDTO crearBloqueDTO(Cuestionario c, BloqueCuestionario b) {
        int total = c.getBloques().size();
        List<PreguntaCuestionarioDTO> preguntas = b.getPreguntas().stream()
                .map(p -> new PreguntaCuestionarioDTO(p.getId(), p.getEnunciado(), p.getOpciones().stream()
                        .map(o -> new ItemOpcionDTO(o.getId(), o.getOrden(), o.getDescripcion()))
                        .toList()))
                .toList();
        return new BloqueDTO(b.getNroBloque(), total, b.getNroBloque() == total, preguntas);
    }

    // el texto del repositorio de configuracion mas los datos de este cuestionario
    private String armarInstrucciones(Cuestionario c) {
        int preguntas = c.getEvaluacion().getCompetenciasEvaluadas().stream()
                .mapToInt(ce -> ce.getFactores().size() * PREGUNTAS_POR_FACTOR)
                .sum();
        int bloques = (preguntas + c.getPreguntasPorBloque() - 1) / c.getPreguntasPorBloque();
        return configuracion.getInstrucciones() + "\n\n"
                + "El cuestionario tiene " + preguntas + " preguntas organizadas en " + bloques + " bloques. "
                + "Tiene " + c.getTiempoMaximoMinutos() + " minutos para completarlo desde que acepta estas instrucciones "
                + "y puede ingresar hasta " + c.getMaxAccesos() + " veces.";
    }

    private String motivoDeCierre(Cuestionario c) {
        return switch (c.getEstado().getNombre()) {
            case EstadoCuestionario.SIN_CONTESTAR -> M33;
            case EstadoCuestionario.INCOMPLETO -> c.superoMaxAccesos() ? M31 : M32;
            default -> NO_DISPONIBLE;
        };
    }

    private CuestionarioDTO dto(Cuestionario c, String mensaje, String instrucciones, BloqueDTO bloque) {
        Puesto p = c.getEvaluacion().getPuesto();
        String puesto = p.getNombre() + " – " + p.getEmpresa().getNombre();
        return new CuestionarioDTO(c.getId(), c.getEstado().getNombre(), mensaje, instrucciones, puesto, bloque);
    }

    private Cuestionario buscar(Long id) {
        return cuestionarioDAO.buscarPorId(id).orElseThrow(() -> new NoSuchElementException("El cuestionario no existe"));
    }
}
