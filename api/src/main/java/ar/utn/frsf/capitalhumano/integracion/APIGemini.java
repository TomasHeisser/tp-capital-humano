package ar.utn.frsf.capitalhumano.integracion;

import ar.utn.frsf.capitalhumano.excepcion.ServicioIAException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Cliente de la API de Google Gemini (el "adaptado" del patron Adapter): manda un prompt
 * a generateContent y devuelve el texto que contesta el modelo. No sabe nada de preguntas.
 */
@Slf4j
public class APIGemini {

    static final String ERROR = "Ocurrió un error al comunicarse con el servicio de IA. Intente nuevamente más tarde";

    private final RestClient cliente;
    private final String modelo;
    private final String clave;
    private final ObjectMapper mapper;

    public APIGemini(String url, String modelo, String clave, int timeoutSegundos, ObjectMapper mapper) {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeoutSegundos)).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(Duration.ofSeconds(timeoutSegundos));
        this.cliente = RestClient.builder().baseUrl(url).requestFactory(fabrica).build();
        this.modelo = modelo;
        this.clave = clave;
        this.mapper = mapper;
    }

    /**
     * Pide al modelo una respuesta en JSON con los campos del esquema.
     *
     * @return el JSON que escribio el modelo, como texto
     * @throws ServicioIAException si no hay conexion, se paso el tiempo o la respuesta no tiene texto
     */
    public String generateContent(String prompt) {
        try {
            String cuerpo = mapper.writeValueAsString(armarPedido(prompt));
            String respuesta = cliente.post()
                    .uri("/models/{modelo}:generateContent", modelo)
                    .header("x-goog-api-key", clave)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(String.class);
            JsonNode texto = mapper.readTree(respuesta).at("/candidates/0/content/parts/0/text");
            if (texto.isMissingNode() || texto.asString().isBlank()) {
                throw new ServicioIAException(ERROR);
            }
            return texto.asString();
        } catch (ServicioIAException e) {
            throw e;
        } catch (RuntimeException e) {
            // RestClientException (sin conexion, timeout, 4xx/5xx) o JSON roto: para el usuario es lo mismo
            log.warn("Fallo el pedido a Gemini: {}", e.getMessage());
            throw new ServicioIAException(ERROR);
        }
    }

    // le pedimos JSON con un esquema fijo, asi no hay que andar limpiando el texto,
    // y que "piense" poco: para esto alcanza y contesta mucho mas rapido
    private Map<String, Object> armarPedido(String prompt) {
        Map<String, Object> texto = Map.of("type", "STRING");
        Map<String, Object> esquema = Map.of(
                "type", "OBJECT",
                "properties", Map.of("nombre", texto, "enunciado", texto, "descripcion", texto, "opcionRespuesta", texto),
                "required", List.of("nombre", "enunciado", "descripcion", "opcionRespuesta"));
        return Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json", "responseSchema", esquema,
                        "thinkingConfig", Map.of("thinkingLevel", "low")));
    }
}
