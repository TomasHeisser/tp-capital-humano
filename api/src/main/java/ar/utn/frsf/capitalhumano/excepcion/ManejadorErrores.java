package ar.utn.frsf.capitalhumano.excepcion;

import ar.utn.frsf.capitalhumano.dto.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Pasa las excepciones a un ErrorDTO con el codigo HTTP que corresponde,
 * asi los controllers no tienen que atrapar nada.
 */
@Slf4j
@RestControllerAdvice
public class ManejadorErrores {

    // codigo segun el tipo de error de negocio; lo que no esta en el mapa es 422
    private static final Map<Class<? extends NegocioException>, HttpStatus> CODIGOS = Map.of(
            ValidacionException.class, HttpStatus.BAD_REQUEST,
            RespuestasIncompletasException.class, HttpStatus.BAD_REQUEST,
            PreguntaDuplicadaException.class, HttpStatus.CONFLICT,
            CompetenciasNoEvaluablesException.class, HttpStatus.UNPROCESSABLE_CONTENT,
            ServicioIAException.class, HttpStatus.SERVICE_UNAVAILABLE);

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorDTO> manejar(NegocioException e) {
        HttpStatus codigo = CODIGOS.getOrDefault(e.getClass(), HttpStatus.UNPROCESSABLE_CONTENT);
        return ResponseEntity.status(codigo).body(new ErrorDTO(e.getMessage(), e.getErrores()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDTO> rutaInexistente(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO("La dirección pedida no existe", List.of()));
    }

    // buscarPorId(...).orElseThrow() cuando no existe
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDTO> noEncontrado(NoSuchElementException e) {
        String mensaje = e.getMessage() == null || e.getMessage().equals("No value present")
                ? "El dato buscado no existe" : e.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDTO(mensaje, List.of()));
    }

    // por ejemplo, querer completar un cuestionario que no esta en proceso
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDTO> estadoInvalido(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(new ErrorDTO(e.getMessage(), List.of()));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorDTO> pedidoMalArmado(Exception e) {
        return ResponseEntity.badRequest().body(new ErrorDTO("Los datos enviados no tienen el formato esperado", List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> inesperado(Exception e) {
        log.error("Error no esperado", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorDTO("Ocurrió un error inesperado. Intente nuevamente", List.of()));
    }
}
