package ar.utn.frsf.capitalhumano.configuracion;

import ar.utn.frsf.capitalhumano.integracion.AdaptadorServicioIA;
import ar.utn.frsf.capitalhumano.integracion.AsistenteIASimulado;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ConfiguracionIATest {

    private final ConfiguracionIA config = new ConfiguracionIA();
    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void conClaveUsaGeminiYSinClaveOForzadoUsaElSimulado() {
        assertInstanceOf(AdaptadorServicioIA.class, config.asistenteIA("gemini", "una-clave", "m", "http://x", 5, mapper));
        assertInstanceOf(AsistenteIASimulado.class, config.asistenteIA("gemini", "  ", "m", "http://x", 5, mapper));
        assertInstanceOf(AsistenteIASimulado.class, config.asistenteIA("SIMULADO", "una-clave", "m", "http://x", 5, mapper));
    }
}
