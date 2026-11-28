package ar.utn.frsf.capitalhumano.reporte;

import ar.utn.frsf.capitalhumano.dominio.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

// arma puestos, evaluaciones y cuestionarios en el estado que haga falta (como los del seed)
final class Datos {

    private static final AtomicLong IDS = new AtomicLong(100);

    final Puesto puesto;
    final Evaluacion evaluacion;
    final CompetenciaEvaluada java;
    final CompetenciaEvaluada gp;
    final CompetenciaEvaluada lealtad;

    Datos() {
        puesto = new Puesto("DEV-JR-01", "DESARROLLADOR JAVA JUNIOR", null, new Empresa("TECNOSUR S.A."));
        puesto.setId(1L);
        evaluacion = new Evaluacion(LocalDateTime.of(2026, 11, 12, 9, 15), puesto, new Consultor("JPEREZ"));
        evaluacion.setId(1L);
        java = competencia("PROGRAMACIÓN JAVA", "6");
        gp = competencia("GESTIÓN DE PROYECTOS", "4");
        lealtad = competencia("LEALTAD", "7");
    }

    private CompetenciaEvaluada competencia(String nombre, String minimo) {
        CompetenciaEvaluada ce = new CompetenciaEvaluada(new Competencia(nombre, nombre, null), new BigDecimal(minimo));
        ce.setId(IDS.incrementAndGet());
        evaluacion.agregarCompetenciaEvaluada(ce);
        return ce;
    }

    static Candidato candidato(String apellido, String nombre, TipoDocumento tipo, String doc) {
        Candidato c = new Candidato();
        c.setId(IDS.incrementAndGet());
        c.setApellido(apellido);
        c.setNombre(nombre);
        c.setTipoDocumento(tipo);
        c.setNroDocumento(doc);
        c.setNroCandidato(IDS.get());
        return c;
    }

    Cuestionario cuestionario(Candidato cand, String estado) {
        Cuestionario c = new Cuestionario(cand, "ABCD1234", 5, 120, 7, 5, evaluacion.getFechaCreacion());
        c.setId(IDS.incrementAndGet());
        c.cambiarEstado(estado, evaluacion.getFechaCreacion());
        evaluacion.agregarCuestionario(c);
        return c;
    }

    // completo con inicio, minutos que tardo, accesos y el puntaje de JAVA, GP y LEALTAD
    Cuestionario completo(Candidato cand, LocalDateTime inicio, int minutos, int accesos, String pJava, String pGp, String pLealtad) {
        Cuestionario c = cuestionario(cand, EstadoCuestionario.COMPLETO);
        c.setFechaInicio(inicio);
        c.setFechaFin(inicio.plusMinutes(minutos));
        c.setUltimoIngreso(inicio);
        c.setCantidadAccesos(accesos);
        c.getResultados().add(new Resultado(java, new BigDecimal(pJava), List.of()));
        c.getResultados().add(new Resultado(gp, new BigDecimal(pGp), List.of()));
        c.getResultados().add(new Resultado(lealtad, new BigDecimal(pLealtad), List.of()));
        return c;
    }
}
