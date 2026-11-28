package ar.utn.frsf.capitalhumano.configuracion;

import ar.utn.frsf.capitalhumano.integracion.APIGemini;
import ar.utn.frsf.capitalhumano.integracion.AdaptadorServicioIA;
import ar.utn.frsf.capitalhumano.integracion.AsistenteIASimulado;
import ar.utn.frsf.capitalhumano.integracion.IAsistenteIA;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/**
 * Elige que asistente de IA usa el sistema: Gemini si hay clave y no se pidio el simulado,
 * y el simulado en cualquier otro caso.
 */
@Slf4j
@Configuration
public class ConfiguracionIA {

    @Bean
    public IAsistenteIA asistenteIA(@Value("${ia.proveedor:gemini}") String proveedor,
                                    @Value("${ia.gemini.clave:}") String clave,
                                    @Value("${ia.gemini.modelo}") String modelo,
                                    @Value("${ia.gemini.url}") String url,
                                    @Value("${ia.gemini.timeout-segundos:30}") int timeout,
                                    ObjectMapper mapper) {
        return elegir(proveedor, clave, modelo, url, timeout, mapper);
    }

    static IAsistenteIA elegir(String proveedor, String clave, String modelo, String url, int timeout, ObjectMapper mapper) {
        if ("simulado".equalsIgnoreCase(proveedor.trim()) || clave == null || clave.isBlank()) {
            log.info("Asistente de IA: simulado");
            return new AsistenteIASimulado();
        }
        log.info("Asistente de IA: Gemini ({})", modelo);
        return new AdaptadorServicioIA(new APIGemini(url, modelo, clave.trim(), timeout, mapper), mapper);
    }
}
