package ar.utn.frsf.capitalhumano.integracion;

import ar.utn.frsf.capitalhumano.excepcion.ServicioIAException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

// levanta un servidor HTTP local que hace de Gemini, asi se prueba el pedido de verdad sin internet
class APIGeminiTest {

    private HttpServer servidor;
    private final AtomicReference<String> cuerpoRecibido = new AtomicReference<>();
    private final AtomicReference<String> claveRecibida = new AtomicReference<>();
    private volatile int codigo = 200;
    private volatile String respuesta = "";

    @BeforeEach
    void levantar() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/v1beta/models/modelo-prueba:generateContent", ex -> {
            cuerpoRecibido.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            claveRecibida.set(ex.getRequestHeaders().getFirst("x-goog-api-key"));
            byte[] b = respuesta.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(codigo, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        servidor.start();
    }

    @AfterEach
    void bajar() {
        servidor.stop(0);
    }

    private APIGemini api() {
        return new APIGemini("http://127.0.0.1:" + servidor.getAddress().getPort() + "/v1beta", "modelo-prueba",
                "clave-de-prueba", 5, JsonMapper.builder().build());
    }

    @Test
    void devuelveElTextoDelPrimerCandidato() {
        respuesta = """
                {"candidates":[{"content":{"parts":[{"text":"{\\"nombre\\":\\"X\\"}"}]}}]}""";

        assertEquals("{\"nombre\":\"X\"}", api().generateContent("hola"));
        assertEquals("clave-de-prueba", claveRecibida.get());
        assertTrue(cuerpoRecibido.get().contains("\"text\":\"hola\""));
        assertTrue(cuerpoRecibido.get().contains("\"responseMimeType\":\"application/json\""));
        assertTrue(cuerpoRecibido.get().contains("\"thinkingLevel\":\"low\""));
    }

    @Test
    void errorDelServicioOSinTextoEsServicioIAException() {
        codigo = 503;
        respuesta = "{\"error\":{\"code\":503,\"status\":\"UNAVAILABLE\"}}";
        ServicioIAException e = assertThrows(ServicioIAException.class, () -> api().generateContent("hola"));
        assertEquals("Ocurrió un error al comunicarse con el servicio de IA. Intente nuevamente más tarde", e.getMessage());

        codigo = 200;
        respuesta = "{\"candidates\":[]}";
        assertThrows(ServicioIAException.class, () -> api().generateContent("hola"));
    }

    @Test
    void sinConexionEsServicioIAException() {
        APIGemini sinServidor = new APIGemini("http://127.0.0.1:1/v1beta", "modelo-prueba", "x", 2, JsonMapper.builder().build());
        assertThrows(ServicioIAException.class, () -> sinServidor.generateContent("hola"));
    }
}
