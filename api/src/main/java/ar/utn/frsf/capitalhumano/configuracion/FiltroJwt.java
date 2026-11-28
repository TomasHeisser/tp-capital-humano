package ar.utn.frsf.capitalhumano.configuracion;

import ar.utn.frsf.capitalhumano.autenticacion.ServicioToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el token del encabezado Authorization y, si es valido, deja al usuario autenticado
 * con su rol. El id del cuestionario del candidato queda en los details.
 * No es @Component a proposito: si no Spring Boot lo registraria dos veces.
 */
public class FiltroJwt extends OncePerRequestFilter {

    private final ServicioToken servicioToken;

    public FiltroJwt(ServicioToken servicioToken) {
        this.servicioToken = servicioToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String encabezado = request.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            String token = encabezado.substring(7);
            servicioToken.obtenerUsuario(token).ifPresent(usuario -> {
                String rol = servicioToken.obtenerRol(token).map(Enum::name).orElse("");
                var auth = new UsernamePasswordAuthenticationToken(usuario, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                auth.setDetails(servicioToken.obtenerIdCuestionario(token).orElse(null));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
}
