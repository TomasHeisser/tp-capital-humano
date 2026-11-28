package ar.utn.frsf.capitalhumano.gestor;

import ar.utn.frsf.capitalhumano.dao.ConsultorDAO;
import ar.utn.frsf.capitalhumano.dao.FactorDAO;
import ar.utn.frsf.capitalhumano.dao.OpcionRespuestaDAO;
import ar.utn.frsf.capitalhumano.dao.PreguntaDAO;
import ar.utn.frsf.capitalhumano.dominio.*;
import ar.utn.frsf.capitalhumano.dto.*;
import ar.utn.frsf.capitalhumano.excepcion.PreguntaDuplicadaException;
import ar.utn.frsf.capitalhumano.excepcion.ValidacionException;
import ar.utn.frsf.capitalhumano.integracion.IAsistenteIA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * CU020 Gestionar preguntas, CU021 Dar de alta, CU022 Modificar, CU023 Dar de baja
 * y CU033 Generar pregunta con IA. Valida, controla duplicados y coordina los DAO.
 */
@Service
public class GestorPreguntas {

    static final String SIN_CRITERIO = "Debe ingresar al menos un criterio de búsqueda";
    static final String NO_EXISTE = "La pregunta no existe o fue dada de baja";
    static final String CORREGIR_IA = "Corrija lo siguiente:";

    private static final Locale ES = Locale.forLanguageTag("es-AR");
    private static final BigDecimal DIEZ = BigDecimal.TEN;

    private final PreguntaDAO preguntaDAO;
    private final FactorDAO factorDAO;
    private final OpcionRespuestaDAO opcionRespuestaDAO;
    private final ConsultorDAO consultorDAO;
    private final GestorAuditoria gestorAuditoria;
    private final IAsistenteIA asistenteIA;
    private final Supplier<LocalDateTime> reloj;

    @Autowired
    public GestorPreguntas(PreguntaDAO preguntaDAO, FactorDAO factorDAO, OpcionRespuestaDAO opcionRespuestaDAO,
                           ConsultorDAO consultorDAO, GestorAuditoria gestorAuditoria, IAsistenteIA asistenteIA) {
        this(preguntaDAO, factorDAO, opcionRespuestaDAO, consultorDAO, gestorAuditoria, asistenteIA, LocalDateTime::now);
    }

    // para los tests: hora fija
    GestorPreguntas(PreguntaDAO preguntaDAO, FactorDAO factorDAO, OpcionRespuestaDAO opcionRespuestaDAO,
                    ConsultorDAO consultorDAO, GestorAuditoria gestorAuditoria, IAsistenteIA asistenteIA,
                    Supplier<LocalDateTime> reloj) {
        this.preguntaDAO = preguntaDAO;
        this.factorDAO = factorDAO;
        this.opcionRespuestaDAO = opcionRespuestaDAO;
        this.consultorDAO = consultorDAO;
        this.gestorAuditoria = gestorAuditoria;
        this.asistenteIA = asistenteIA;
        this.reloj = reloj;
    }

    /**
     * CU020 pasos 3 y 4: busca las preguntas activas. Hace falta al menos un criterio;
     * el nombre se compara con "contiene" sin distinguir mayusculas.
     */
    @Transactional(readOnly = true)
    public List<PreguntaResumenDTO> buscarPreguntas(FiltroPreguntaDTO filtro) {
        boolean hayCriterio = filtro != null && (filtro.getIdCompetencia() != null || filtro.getIdFactor() != null
                || (filtro.getNombre() != null && !filtro.getNombre().isBlank()));
        if (!hayCriterio) {
            throw new ValidacionException(SIN_CRITERIO);
        }
        List<Pregunta> preguntas = preguntaDAO.buscarPorFiltros(filtro.getIdCompetencia(), filtro.getIdFactor(), filtro.getNombre());
        List<PreguntaResumenDTO> resultado = new ArrayList<>();
        for (Pregunta p : preguntas) {
            Factor f = p.getFactor();
            Competencia c = f.getCompetencia();
            String nombreCompetencia = c.getNombre();
            resultado.add(new PreguntaResumenDTO(p.getId(), nombreCompetencia, f.getNombre(), p.getNombre(), p.getEnunciado()));
        }
        return resultado;
    }

    /** CU022 paso 1: datos de la pregunta con la ponderacion de cada opcion, por orden. */
    @Transactional(readOnly = true)
    public PreguntaDTO obtenerPregunta(Long id) {
        Pregunta p = buscarActiva(id);
        List<PonderacionDTO> ponderaciones = p.getOpcionRespuesta().getItems().stream()
                .sorted(Comparator.comparing(ItemOpcionRespuesta::getOrden))
                .map(i -> new PonderacionDTO(i.getId(), i.getOrden(), i.getDescripcion(), p.getPonderacion(i)))
                .toList();
        return new PreguntaDTO(p.getId(), p.getFactor().getCompetencia().getId(), p.getFactor().getId(), p.getNombre(),
                p.getEnunciado(), p.getDescripcion(), p.getOpcionRespuesta().getId(), new ArrayList<>(ponderaciones));
    }

    /**
     * CU021 pasos 2 a 4: valida todo junto, controla que no haya otra pregunta activa con el
     * mismo nombre o enunciado en el factor y guarda la pregunta con sus ponderaciones.
     */
    @Transactional
    public boolean registrarPregunta(PreguntaDTO datos) {
        normalizar(datos);
        List<String> errores = validar(datos);
        if (!errores.isEmpty()) {
            throw new ValidacionException(tituloErrores("registrar", errores), errores);
        }
        List<Pregunta> coincidencias = preguntaDAO.buscarCoincidencias(datos.getIdFactor(), datos.getNombre(), datos.getEnunciado(), null);
        if (!coincidencias.isEmpty()) {
            throw duplicada("registrar", coincidencias, datos);
        }
        Factor f = factorDAO.buscarPorId(datos.getIdFactor())
                .orElseThrow(() -> new NoSuchElementException("El factor no existe"));
        OpcionRespuesta op = opcionRespuestaDAO.buscarPorId(datos.getIdOpcionRespuesta())
                .orElseThrow(() -> new NoSuchElementException("La opción de respuesta no existe"));
        Pregunta p = new Pregunta(datos.getNombre(), datos.getEnunciado(), datos.getDescripcion(), f, op);
        for (PonderacionDTO pd : datos.getPonderaciones()) {
            ItemOpcionRespuesta item = op.getItem(pd.getIdItem());
            p.agregarPonderacion(item, pd.getPonderacion());
        }
        controlarOpcionesPonderadas(p, op, "registrar");
        return preguntaDAO.guardar(p);
    }

    /**
     * CU022: igual que el alta pero sobre una pregunta existente. La competencia y el factor
     * no se cambian; el duplicado se controla sin contar a la misma pregunta.
     */
    @Transactional
    public boolean modificarPregunta(PreguntaDTO datos) {
        Pregunta p = buscarActiva(datos.getId());
        // competencia y factor son de solo lectura: se usan los que ya tiene
        datos.setIdFactor(p.getFactor().getId());
        datos.setIdCompetencia(p.getFactor().getCompetencia().getId());
        normalizar(datos);
        List<String> errores = validar(datos);
        if (!errores.isEmpty()) {
            throw new ValidacionException(tituloErrores("modificar", errores), errores);
        }
        List<Pregunta> coincidencias = preguntaDAO.buscarCoincidencias(datos.getIdFactor(), datos.getNombre(), datos.getEnunciado(), p.getId());
        if (!coincidencias.isEmpty()) {
            throw duplicada("modificar", coincidencias, datos);
        }
        OpcionRespuesta op = opcionRespuestaDAO.buscarPorId(datos.getIdOpcionRespuesta())
                .orElseThrow(() -> new NoSuchElementException("La opción de respuesta no existe"));
        p.setNombre(datos.getNombre());
        p.setEnunciado(datos.getEnunciado());
        p.setDescripcion(datos.getDescripcion());
        p.setOpcionRespuesta(op);
        actualizarPonderaciones(p, op, datos.getPonderaciones());
        controlarOpcionesPonderadas(p, op, "modificar");
        return preguntaDAO.actualizar(p);
    }

    /**
     * CU023 paso 3: baja logica de la pregunta y registro de auditoria con el consultor,
     * todo en la misma transaccion.
     */
    @Transactional
    public boolean darDeBajaPregunta(Long id, String username) {
        Pregunta p = preguntaDAO.buscarPorId(id)
                .filter(x -> x.getFechaBaja() == null)
                .orElseThrow(() -> new NoSuchElementException(NO_EXISTE));
        Consultor consultor = consultorDAO.buscarPorUsername(username)
                .orElseThrow(() -> new NoSuchElementException("El consultor " + username + " no existe"));
        p.darDeBaja(reloj.get());
        preguntaDAO.actualizar(p);
        return gestorAuditoria.registrarBaja("BAJA", "PREGUNTA", p.getId(), consultor);
    }

    /**
     * CU033: le pide al asistente de IA una pregunta para el factor, mandandole tambien
     * el catalogo de opciones de respuesta para que sugiera una.
     */
    @Transactional(readOnly = true)
    public PreguntaDTO generarPreguntaIA(SolicitudIADTO s) {
        List<String> errores = validar(s);
        if (!errores.isEmpty()) {
            throw new ValidacionException(CORREGIR_IA, errores);
        }
        Factor f = factorDAO.buscarPorId(s.getIdFactor())
                .orElseThrow(() -> new NoSuchElementException("El factor no existe"));
        Competencia competencia = f.getCompetencia();
        List<OpcionRespuesta> opciones = opcionRespuestaDAO.buscarTodos();
        return asistenteIA.generarPregunta(competencia, f, opciones);
    }

    // ------------------------------------------------------------------ validaciones

    /** Junta todas las omisiones, longitudes excedidas y errores de ponderacion. */
    private List<String> validar(PreguntaDTO datos) {
        List<String> errores = new ArrayList<>();
        if (datos.getIdCompetencia() == null) errores.add("Debe seleccionar la Competencia.");
        if (datos.getIdFactor() == null) errores.add("Debe seleccionar el Factor.");
        texto(errores, datos.getNombre(), "el", "Nombre de pregunta", 100, true);
        texto(errores, datos.getEnunciado(), "la", "Pregunta", 500, true);
        texto(errores, datos.getDescripcion(), "la", "Descripción", 500, false);
        if (datos.getIdOpcionRespuesta() == null) {
            errores.add("Debe seleccionar la Opción de respuesta.");
            return errores;
        }
        List<PonderacionDTO> ponderaciones = datos.getPonderaciones() == null ? List.of() : datos.getPonderaciones();
        boolean completas = !ponderaciones.isEmpty();
        for (PonderacionDTO pd : ponderaciones) {
            if (pd.getPonderacion() == null) {
                errores.add("Debe ingresar la Ponderación de la opción " + descripcion(pd) + ".");
                completas = false;
            } else if (pd.getPonderacion().stripTrailingZeros().scale() > 2) {
                errores.add("La Ponderación de la opción " + descripcion(pd) + " admite hasta 2 decimales.");
            }
        }
        if (ponderaciones.isEmpty()) {
            errores.add("Debe ingresar la Ponderación de cada opción.");
        }
        if (completas) {
            BigDecimal suma = ponderaciones.stream().map(PonderacionDTO::getPonderacion).reduce(BigDecimal.ZERO, BigDecimal::add);
            boolean hayNegativas = ponderaciones.stream().anyMatch(pd -> pd.getPonderacion().signum() < 0);
            if (hayNegativas || suma.compareTo(DIEZ) != 0) {
                errores.add(mensajePonderaciones(suma));
            }
        }
        return errores;
    }

    private List<String> validar(SolicitudIADTO s) {
        List<String> errores = new ArrayList<>();
        if (s == null || s.getIdCompetencia() == null) errores.add("Debe seleccionar la Competencia.");
        if (s == null || s.getIdFactor() == null) errores.add("Debe seleccionar el Factor.");
        return errores;
    }

    // "Debe ingresar el Nombre de pregunta." / "El Nombre de pregunta excede la longitud maxima..."
    private static void texto(List<String> errores, String valor, String articulo, String campo, int largo, boolean obligatorio) {
        if (valor == null || valor.isBlank()) {
            if (obligatorio) errores.add("Debe ingresar " + articulo + " " + campo + ".");
        } else if (valor.length() > largo) {
            String articuloMayuscula = Character.toUpperCase(articulo.charAt(0)) + articulo.substring(1);
            errores.add(articuloMayuscula + " " + campo + " excede la longitud máxima de " + largo
                    + " caracteres (tiene " + valor.length() + ").");
        }
    }

    static String mensajePonderaciones(BigDecimal suma) {
        String s = suma.stripTrailingZeros().toPlainString().replace('.', ',');
        return "Las ponderaciones deben ser 0 o positivas y sumar 10 (actualmente suman " + s + ").";
    }

    // si solo fallan las ponderaciones el panel no dice "Corrija lo siguiente" (igual que en los wireframes)
    private static String tituloErrores(String operacion, List<String> errores) {
        boolean soloPonderaciones = errores.stream().allMatch(e -> e.startsWith("Las ponderaciones"));
        return soloPonderaciones ? "No se pudo " + operacion + " la pregunta:"
                : "No se pudo " + operacion + " la pregunta. Corrija lo siguiente:";
    }

    // CU021/CU022 2.C: el mensaje dice si coincide el nombre, el enunciado o los dos
    private static PreguntaDuplicadaException duplicada(String operacion, List<Pregunta> coincidencias, PreguntaDTO datos) {
        boolean mismoNombre = coincidencias.stream().anyMatch(p -> p.getNombre().equalsIgnoreCase(datos.getNombre()));
        boolean mismoEnunciado = coincidencias.stream().anyMatch(p -> p.getEnunciado().equalsIgnoreCase(datos.getEnunciado()));
        Factor f = coincidencias.getFirst().getFactor();
        String cual = mismoNombre && mismoEnunciado ? "con el nombre " + datos.getNombre() + " y con ese enunciado"
                : mismoNombre ? "con el nombre " + datos.getNombre() : "con ese enunciado";
        String mensaje = "Ya existe una pregunta " + cual + " para la competencia " + f.getCompetencia().getNombre()
                + " y el factor " + f.getNombre() + ".";
        return new PreguntaDuplicadaException("No se pudo " + operacion + " la pregunta:", List.of(mensaje));
    }

    // todas las opciones del conjunto tienen que tener su ponderacion (una sola vez cada una)
    private static void controlarOpcionesPonderadas(Pregunta p, OpcionRespuesta op, String operacion) {
        long distintas = p.getPonderaciones().stream().map(pp -> pp.getItem().getId()).distinct().count();
        if (distintas != op.getItems().size() || p.getPonderaciones().size() != op.getItems().size()) {
            throw new ValidacionException("No se pudo " + operacion + " la pregunta. Corrija lo siguiente:",
                    List.of("Debe ingresar la Ponderación de cada opción de " + op.getNombre() + "."));
        }
    }

    // se actualizan en el lugar las que siguen, se agregan las nuevas y se sacan las del conjunto anterior
    // (si se borraran todas y se volvieran a crear, Hibernate inserta antes de borrar y choca la clave)
    private static void actualizarPonderaciones(Pregunta p, OpcionRespuesta op, List<PonderacionDTO> nuevas) {
        Set<Long> idsNuevos = new HashSet<>();
        for (PonderacionDTO pd : nuevas) {
            ItemOpcionRespuesta item = op.getItem(pd.getIdItem());
            idsNuevos.add(item.getId());
            p.getPonderaciones().stream()
                    .filter(pp -> pp.getItem().getId().equals(item.getId()))
                    .findFirst()
                    .ifPresentOrElse(pp -> pp.setPonderacion(pd.getPonderacion()),
                            () -> p.agregarPonderacion(item, pd.getPonderacion()));
        }
        p.getPonderaciones().removeIf(pp -> !idsNuevos.contains(pp.getItem().getId()));
    }

    // los literales van siempre en mayusculas y sin espacios de mas
    private static void normalizar(PreguntaDTO d) {
        d.setNombre(mayusculas(d.getNombre()));
        d.setEnunciado(mayusculas(d.getEnunciado()));
        String desc = mayusculas(d.getDescripcion());
        d.setDescripcion(desc == null || desc.isEmpty() ? null : desc);
    }

    private static String mayusculas(String s) {
        return s == null ? null : s.trim().toUpperCase(ES);
    }

    private static String descripcion(PonderacionDTO pd) {
        return pd.getDescripcion() != null ? pd.getDescripcion() : String.valueOf(pd.getOrden());
    }

    private Pregunta buscarActiva(Long id) {
        return preguntaDAO.buscarPorId(id)
                .filter(p -> p.getFechaBaja() == null)
                .orElseThrow(() -> new NoSuchElementException(NO_EXISTE));
    }
}
