package ar.utn.frsf.capitalhumano.autenticacion;

import ar.utn.frsf.capitalhumano.dominio.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Arma y valida los JWT de la sesion. El token lleva el usuario, el rol y,
 * para el candidato, el id del cuestionario que puede completar.
 */
@Component
public class ServicioToken {

    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_CUESTIONARIO = "idCuestionario";

    private final SecretKey clave;
    private final Duration duracion;

    public ServicioToken(@Value("${jwt.secreto:}") String secreto,
                         @Value("${jwt.secreto-desarrollo}") String secretoDesarrollo,
                         @Value("${jwt.duracion-horas:8}") long horas) {
        // si no configuraron secreto propio usamos el de desarrollo
        String elegido = secreto == null || secreto.isBlank() ? secretoDesarrollo : secreto;
        this.clave = Keys.hmacShaKeyFor(alMenos32Bytes(elegido));
        this.duracion = Duration.ofHours(horas);
    }

    public String generarToken(String usuario, Rol rol) {
        return generarToken(usuario, rol, null);
    }

    public String generarToken(String usuario, Rol rol, Long idCuestionario) {
        Instant ahora = Instant.now();
        var builder = Jwts.builder()
                .subject(usuario)
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave);
        if (idCuestionario != null) {
            builder.claim(CLAIM_CUESTIONARIO, idCuestionario);
        }
        return builder.compact();
    }

    public boolean validarToken(String token) {
        return leer(token).isPresent();
    }

    public Optional<String> obtenerUsuario(String token) {
        return leer(token).map(Claims::getSubject);
    }

    public Optional<Rol> obtenerRol(String token) {
        return leer(token).map(c -> Rol.valueOf(c.get(CLAIM_ROL, String.class)));
    }

    public Optional<Long> obtenerIdCuestionario(String token) {
        return leer(token).map(c -> c.get(CLAIM_CUESTIONARIO, Long.class));
    }

    private Optional<Claims> leer(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    // HS256 pide 256 bits; si el secreto es corto lo repetimos
    private static byte[] alMenos32Bytes(String secreto) {
        byte[] b = secreto.getBytes(StandardCharsets.UTF_8);
        if (b.length >= 32) {
            return b;
        }
        byte[] r = new byte[32];
        for (int i = 0; i < r.length; i++) {
            r[i] = b[i % b.length];
        }
        return r;
    }
}
