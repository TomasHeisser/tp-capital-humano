package ar.utn.frsf.capitalhumano.integracion;

import ar.utn.frsf.capitalhumano.dominio.Competencia;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import ar.utn.frsf.capitalhumano.dominio.ItemOpcionRespuesta;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import ar.utn.frsf.capitalhumano.dto.PreguntaDTO;
import ar.utn.frsf.capitalhumano.excepcion.ServicioIAException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Adapter entre el sistema y Gemini: arma el prompt con la competencia, el factor y el catalogo
 * de opciones de respuesta, y convierte el JSON que vuelve en un PreguntaDTO.
 */
public class AdaptadorServicioIA implements IAsistenteIA {

    private static final Locale ES = Locale.forLanguageTag("es-AR");

    private final APIGemini api;
    private final ObjectMapper mapper;

    public AdaptadorServicioIA(APIGemini api, ObjectMapper mapper) {
        this.api = api;
        this.mapper = mapper;
    }

    @Override
    public PreguntaDTO generarPregunta(Competencia comp, Factor factor, List<OpcionRespuesta> opciones) {
        String prompt = armarPrompt(comp, factor, opciones);
        String json = api.generateContent(prompt);
        return convertir(json, comp, factor, opciones);
    }

    String armarPrompt(Competencia comp, Factor factor, List<OpcionRespuesta> opciones) {
        String catalogo = opciones.stream()
                .map(o -> "- " + o.getNombre() + ": " + o.getItems().stream()
                        .map(ItemOpcionRespuesta::getDescripcion)
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n"));
        return """
                Sos un especialista en selección de personal. Proponé UNA pregunta para un cuestionario \
                de evaluación de candidatos que mida el factor indicado de la competencia indicada.
                Competencia: %s (%s)
                Factor: %s (%s)
                Conjuntos de opciones de respuesta disponibles (nombre: opciones):
                %s
                Respondé en español, todo en MAYÚSCULAS, con un JSON con estos campos:
                "nombre" (título corto de la pregunta, hasta 100 caracteres),
                "enunciado" (la pregunta que lee el candidato, hasta 500 caracteres),
                "descripcion" (para qué sirve la pregunta, hasta 500 caracteres) y
                "opcionRespuesta" (el nombre exacto de uno de los conjuntos de arriba que mejor se adapte).
                """.formatted(comp.getNombre(), texto(comp.getDescripcion()), factor.getNombre(),
                texto(factor.getDescripcion()), catalogo);
    }

    // el JSON del modelo -> PreguntaDTO; si no tiene lo minimo lo tomamos como error del servicio
    private PreguntaDTO convertir(String json, Competencia comp, Factor factor, List<OpcionRespuesta> opciones) {
        try {
            JsonNode n = mapper.readTree(json);
            String nombre = campo(n, "nombre", 100);
            String enunciado = campo(n, "enunciado", 500);
            if (nombre.isEmpty() || enunciado.isEmpty()) {
                throw new ServicioIAException(APIGemini.ERROR);
            }
            Long idOpcion = buscarConjunto(campo(n, "opcionRespuesta", 500), opciones);
            String descripcion = campo(n, "descripcion", 500);
            return new PreguntaDTO(null, comp.getId(), factor.getId(), nombre, enunciado,
                    descripcion.isEmpty() ? null : descripcion, idOpcion, new ArrayList<>());
        } catch (JacksonException e) {
            throw new ServicioIAException(APIGemini.ERROR);
        }
    }

    // a veces el modelo agrega algo al nombre ("ESCALA DE 5 NIVELES (MALO, ...)") o cambia los espacios,
    // asi que se compara solo con letras y numeros; si no coincide ninguno, el consultor lo elige a mano
    private static Long buscarConjunto(String sugerido, List<OpcionRespuesta> opciones) {
        String s = clave(sugerido);
        if (s.isEmpty()) {
            return null;
        }
        return opciones.stream()
                .filter(o -> !clave(o.getNombre()).isEmpty() && s.contains(clave(o.getNombre())))
                .max(Comparator.comparingInt(o -> clave(o.getNombre()).length()))
                .map(OpcionRespuesta::getId)
                .orElse(null);
    }

    private static String clave(String s) {
        return s == null ? "" : s.toUpperCase(ES).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    // texto en mayusculas y recortado al largo del campo, asi entra en el formulario
    private static String campo(JsonNode n, String nombre, int largo) {
        String v = n.path(nombre).asString("").trim().toUpperCase(ES);
        return v.length() > largo ? v.substring(0, largo).trim() : v;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? "sin descripción" : s;
    }
}
