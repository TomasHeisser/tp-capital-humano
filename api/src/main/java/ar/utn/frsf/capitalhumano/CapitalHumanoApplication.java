package ar.utn.frsf.capitalhumano;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

/**
 * Arranque de la API de Capit@l Humano.
 */
@SpringBootApplication
@EnableScheduling
public class CapitalHumanoApplication {

    public static void main(String[] args) {
        // todas las fechas del sistema van en hora argentina
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Buenos_Aires"));
        SpringApplication.run(CapitalHumanoApplication.class, args);
    }
}
