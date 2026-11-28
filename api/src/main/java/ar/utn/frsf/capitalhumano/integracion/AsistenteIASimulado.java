package ar.utn.frsf.capitalhumano.integracion;

import ar.utn.frsf.capitalhumano.dominio.Competencia;
import ar.utn.frsf.capitalhumano.dominio.Factor;
import ar.utn.frsf.capitalhumano.dominio.OpcionRespuesta;
import ar.utn.frsf.capitalhumano.dto.PreguntaDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntUnaryOperator;

/**
 * Asistente "de mentira" que arma la pregunta con plantillas. Se usa cuando no hay clave de
 * Gemini o se fuerza con ia.proveedor=simulado, asi el CU se puede probar sin internet.
 */
public class AsistenteIASimulado implements IAsistenteIA {

    // {f} = factor, {c} = competencia; el conjunto preferido de cada plantilla va al final
    private static final String[][] PLANTILLAS = {
            {"AUTOEVALUACIÓN EN {f}", "¿CÓMO CALIFICA SU DESEMPEÑO EN {f} DENTRO DE {c}?", "ESCALA DE 5 NIVELES"},
            {"EXPERIENCIA EN {f}", "¿CON QUÉ FRECUENCIA APLICÓ {f} EN SUS ÚLTIMOS PROYECTOS?", "ESCALA DE 5 NIVELES"},
            {"ACTITUD FRENTE A {f}", "CONSIDERO QUE {f} ES CLAVE PARA UN BUEN DESEMPEÑO EN {c}.", "GRADO DE ACUERDO"},
            {"CONOCIMIENTO DE {f}", "¿{f} FORMA PARTE DE LAS PRÁCTICAS HABITUALES DE {c}?", "VERDADERO / FALSO"},
    };

    private final IntUnaryOperator azar;

    public AsistenteIASimulado() {
        this(new Random()::nextInt);
    }

    // para los tests: se elige que plantilla sale
    AsistenteIASimulado(IntUnaryOperator azar) {
        this.azar = azar;
    }

    @Override
    public PreguntaDTO generarPregunta(Competencia comp, Factor factor, List<OpcionRespuesta> opciones) {
        String[] p = PLANTILLAS[azar.applyAsInt(PLANTILLAS.length)];
        String nombre = recortar(p[0].replace("{f}", factor.getNombre()), 100);
        String enunciado = recortar(p[1].replace("{f}", factor.getNombre()).replace("{c}", comp.getNombre()), 500);
        String descripcion = "PREGUNTA SUGERIDA PARA EVALUAR EL FACTOR " + factor.getNombre() + ".";
        // el conjunto de la plantilla si existe; si no, el primero del catalogo
        Long idOpcion = opciones.stream()
                .filter(o -> o.getNombre().equalsIgnoreCase(p[2]))
                .findFirst()
                .or(() -> opciones.stream().findFirst())
                .map(OpcionRespuesta::getId)
                .orElse(null);
        return new PreguntaDTO(null, comp.getId(), factor.getId(), nombre, enunciado, recortar(descripcion, 500),
                idOpcion, new ArrayList<>());
    }

    private static String recortar(String s, int largo) {
        return s.length() > largo ? s.substring(0, largo).trim() : s;
    }
}
