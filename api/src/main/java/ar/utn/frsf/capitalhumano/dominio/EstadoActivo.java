package ar.utn.frsf.capitalhumano.dominio;

import java.time.LocalDateTime;

/**
 * Creado y todavia sin empezar. Vence por vigencia (pasa a Sin contestar)
 * o por exceso de accesos (pasa a Incompleto).
 */
public class EstadoActivo extends EstadoCuestionario {

    @Override
    public boolean registrarAcceso(Cuestionario c, LocalDateTime ahora) {
        c.sumarAcceso(ahora);
        if (c.superoVigencia(ahora)) {
            c.cambiarEstado(SIN_CONTESTAR, ahora);
            return false;
        }
        if (c.superoMaxAccesos()) {
            c.cambiarEstado(INCOMPLETO, ahora);
            return false;
        }
        return true;
    }

    // acepto las instrucciones: desde aca corre el tiempo maximo
    @Override
    public void iniciar(Cuestionario c, LocalDateTime ahora) {
        c.setFechaInicio(ahora);
        c.cambiarEstado(EN_PROCESO, ahora);
    }

    @Override
    public boolean verificarVencimiento(Cuestionario c, LocalDateTime ahora) {
        if (c.superoVigencia(ahora)) {
            c.cambiarEstado(SIN_CONTESTAR, ahora);
            return true;
        }
        return false;
    }

    @Override
    public String getNombre() {
        return ACTIVO;
    }
}
