package ar.utn.frsf.capitalhumano.dominio;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chequeos propios de las entidades (los que no son del cuestionario).
 */
class DominioTest {

    private OpcionRespuesta verdaderoFalso() {
        OpcionRespuesta op = new OpcionRespuesta("VERDADERO / FALSO", "DOS OPCIONES");
        op.agregarItem(1, "VERDADERO");
        op.agregarItem(2, "FALSO");
        op.getItems().get(0).setId(10L);
        op.getItems().get(1).setId(11L);
        return op;
    }

    private Pregunta pregunta(Factor f, OpcionRespuesta op) {
        Pregunta p = new Pregunta("WBS EN EL ALCANCE", "¿LA WBS FORMA PARTE DEL ALCANCE?", null, f, op);
        p.agregarPonderacion(op.getItem(10L), new BigDecimal("10"));
        p.agregarPonderacion(op.getItem(11L), BigDecimal.ZERO);
        return p;
    }

    @Test
    void opcionRespuestaDevuelveSuItemOFalla() {
        OpcionRespuesta op = verdaderoFalso();
        assertEquals("FALSO", op.getItem(11L).getDescripcion());
        assertThrows(NoSuchElementException.class, () -> op.getItem(99L));
    }

    @Test
    void preguntaGuardaPonderacionesYSeDaDeBaja() {
        OpcionRespuesta op = verdaderoFalso();
        Pregunta p = pregunta(new Factor(), op);
        assertEquals(2, p.getPonderaciones().size());
        assertEquals(new BigDecimal("10"), p.getPonderacion(op.getItem(10L)));
        LocalDateTime ahora = LocalDateTime.now();
        p.darDeBaja(ahora);
        assertEquals(ahora, p.getFechaBaja());
    }

    @Test
    void factorSoloDevuelveLasPreguntasActivas() {
        Factor f = new Factor("FAC-1", "ALCANCE", null, 1, new Competencia("C", "GP", null));
        Pregunta activa = pregunta(f, verdaderoFalso());
        Pregunta baja = pregunta(f, verdaderoFalso());
        baja.darDeBaja(LocalDateTime.now());
        f.getPreguntas().addAll(List.of(activa, baja));
        assertEquals(List.of(activa), f.getPreguntasActivas());
    }

    @Test
    void laCopiaDelModeloCongelaPreguntaYPonderaciones() {
        Competencia comp = new Competencia("COMP-GP", "GESTIÓN DE PROYECTOS", null);
        Factor f = new Factor("FAC-GP-ALC", "GESTIÓN DEL ALCANCE", null, 1, comp);
        Pregunta p = pregunta(f, verdaderoFalso());

        CompetenciaEvaluada ce = new CompetenciaEvaluada(comp, new BigDecimal("4"));
        FactorEvaluado fe = new FactorEvaluado(f);
        PreguntaEvaluada pe = new PreguntaEvaluada(p);
        fe.agregarPregunta(pe);
        ce.agregarFactor(fe);
        Evaluacion ev = new Evaluacion(LocalDateTime.now(), new Puesto(), new Consultor("JPEREZ"));
        ev.agregarCompetenciaEvaluada(ce);

        // despues de copiar, cambiar la original no afecta la copia
        p.setEnunciado("OTRO");
        assertEquals("¿LA WBS FORMA PARTE DEL ALCANCE?", pe.getEnunciado());
        assertEquals(2, pe.getOpciones().size());
        assertEquals(new BigDecimal("10"), pe.getOpciones().get(0).getPonderacion());
        assertEquals("COMP-GP", ce.getCodigo());
        assertEquals(1, fe.getNroOrden());
        assertSame(ev, ce.getEvaluacion());
        assertSame(fe, pe.getFactorEvaluado());

        pe.getOpciones().get(1).setId(5L);
        assertEquals("FALSO", pe.getOpcion(5L).getDescripcion());
        assertThrows(NoSuchElementException.class, () -> pe.getOpcion(6L));
    }

    @Test
    void elBloqueQuedaRespondidoCuandoTieneTodasLasRespuestas() {
        PreguntaEvaluada p1 = new PreguntaEvaluada();
        p1.setId(1L);
        PreguntaEvaluada p2 = new PreguntaEvaluada();
        p2.setId(2L);
        BloqueCuestionario b = new BloqueCuestionario(1, List.of(p1, p2));
        OpcionEvaluada o = new OpcionEvaluada(p1, 1, "SI", BigDecimal.TEN);

        b.registrarRespuesta(p1, o, LocalDateTime.now());
        assertFalse(b.estaRespondido());
        b.registrarRespuesta(p2, o, LocalDateTime.now());
        assertTrue(b.estaCompleto());
        assertTrue(b.estaRespondido());
        assertEquals(2L, b.getRespuestas().get(1).getIdPreguntaEvaluada());
    }

    @Test
    void puestoAgregaCompetenciasYUsuariosDicenSuRol() {
        Puesto p = new Puesto("DEV", "DESARROLLADOR", null, new Empresa("TECNOSUR S.A."));
        p.agregarCompetencia(new Competencia("C", "JAVA", null), new BigDecimal("6"));
        assertEquals(1, p.getCompetenciasRequeridas().size());
        assertSame(p, p.getCompetenciasRequeridas().get(0).getPuesto());
        assertEquals(Rol.CONSULTOR, new Consultor("JPEREZ").getRol());
        assertEquals(Rol.CANDIDATO, new Candidato().getRol());
        assertEquals(AccionEjecucion.INICIO, new RegistroEjecucion(LocalDateTime.now(), AccionEjecucion.INICIO).getAccion());
    }
}
