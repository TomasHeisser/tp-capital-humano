package ar.utn.frsf.capitalhumano.configuracion;

import ar.utn.frsf.capitalhumano.autenticacion.ServicioToken;
import ar.utn.frsf.capitalhumano.dto.ErrorDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * Quien puede entrar a cada ruta: el login es libre, el candidato solo a su cuestionario
 * y todo lo demas es del consultor. Sin sesiones de servidor: todo va con el JWT.
 */
@Configuration
public class ConfiguracionSeguridad {

    private final FiltroJwt filtroJwt;
    private final ObjectMapper mapper;
    private final List<String> origenes;

    public ConfiguracionSeguridad(ServicioToken servicioToken, ObjectMapper mapper, @Value("${cors.origenes}") List<String> origenes) {
        this.filtroJwt = new FiltroJwt(servicioToken);
        this.mapper = mapper;
        this.origenes = origenes;
    }

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http) throws Exception {
        http
                .csrf(c -> c.disable())
                .cors(c -> c.configurationSource(cors()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/login", "/error").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/cuestionarios/{id}/**").access(soloSuCuestionario())
                        .requestMatchers("/api/**").hasRole("CONSULTOR")
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                responder(res, HttpServletResponse.SC_UNAUTHORIZED, "Debe iniciar sesión para continuar"))
                        .accessDeniedHandler((req, res, ex) ->
                                responder(res, HttpServletResponse.SC_FORBIDDEN, "No tiene permiso para realizar esta operación")))
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // el candidato solo puede tocar el cuestionario que figura en su token
    private AuthorizationManager<RequestAuthorizationContext> soloSuCuestionario() {
        return (Supplier<? extends Authentication> auth, RequestAuthorizationContext ctx) -> {
            Authentication a = auth.get();
            boolean esCandidato = a != null && a.getAuthorities().stream()
                    .anyMatch(g -> g.getAuthority().equals("ROLE_CANDIDATO"));
            String id = ctx.getVariables().get("id");
            return new AuthorizationDecision(esCandidato && a.getDetails() != null && a.getDetails().toString().equals(id));
        };
    }

    @Bean
    public CorsConfigurationSource cors() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOrigins(origenes);
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setExposedHeaders(List.of("Content-Disposition"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", c);
        return fuente;
    }

    private void responder(HttpServletResponse res, int codigo, String mensaje) throws IOException {
        res.setStatus(codigo);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), new ErrorDTO(mensaje, List.of()));
    }
}
