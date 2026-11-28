package ar.utn.frsf.capitalhumano.configuracion;

import ar.utn.frsf.capitalhumano.gestor.GestorCuestionario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * CU032: cada tanto (cuestionario.tarea.intervalo-ms) finaliza los cuestionarios vencidos.
 * No tiene pantalla; el registro de inicio y fin lo deja el gestor.
 */
@Slf4j
@Component
public class TareaFinalizarCuestionarios {

    private final GestorCuestionario gestor;

    public TareaFinalizarCuestionarios(GestorCuestionario gestor) {
        this.gestor = gestor;
    }

    @Scheduled(initialDelayString = "${cuestionario.tarea.intervalo-ms}", fixedDelayString = "${cuestionario.tarea.intervalo-ms}")
    public void ejecutar() {
        try {
            int cambiados = gestor.finalizarVencidos();
            log.info("Finalizar cuestionarios vencidos: {} cambiaron de estado", cambiados);
        } catch (RuntimeException e) {
            // si falla una corrida no se corta la tarea, se reintenta en la proxima
            log.error("Falló la tarea de cuestionarios vencidos", e);
        }
    }
}
