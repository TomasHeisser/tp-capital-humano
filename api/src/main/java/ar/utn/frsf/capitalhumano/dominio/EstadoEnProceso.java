package ar.utn.frsf.capitalhumano.dominio;

import java.time.LocalDateTime;

/**
 * Ya empezo a responder. Pasa a Incompleto si se pasa de accesos o del tiempo maximo,
 * y a Completo cuando responde el ultimo bloque.
 */
public class EstadoEnProceso extends EstadoCuestionario {

    @Override
    public boolean registrarAcceso(Cuestionario c, LocalDateTime ahora) {
        c.sumarAcceso(ahora);
        if (c.superoMaxAccesos() || c.superoTiempoMaximo(ahora)) {
            c.cambiarEstado(INCOMPLETO, ahora);
            return false;
        }
        return true;
    }

    @Override
    public void completar(Cuestionario c, LocalDateTime ahora) {
        c.setFechaFin(ahora);
        c.cambiarEstado(COMPLETO, ahora);
    }

    @Override
    public boolean verificarVencimiento(Cuestionario c, LocalDateTime ahora) {
        if (c.superoTiempoMaximo(ahora)) {
            c.cambiarEstado(INCOMPLETO, ahora);
            return true;
        }
        return false;
    }

    @Override
    public String getNombre() {
        return EN_PROCESO;
    }
}
