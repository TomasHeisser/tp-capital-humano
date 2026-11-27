package ar.utn.frsf.capitalhumano.controlador;

import ar.utn.frsf.capitalhumano.dto.CredencialesDTO;
import ar.utn.frsf.capitalhumano.dto.SesionDTO;
import ar.utn.frsf.capitalhumano.gestor.GestorAutenticacion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CU001 - Autenticar usuario")
@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final GestorAutenticacion gestor;

    public AutenticacionController(GestorAutenticacion gestor) {
        this.gestor = gestor;
    }

    @Operation(summary = "Login del consultor (LDAP) o del candidato (documento y clave)")
    @PostMapping("/login")
    public SesionDTO login(@RequestBody CredencialesDTO cred) {
        return gestor.autenticar(cred);
    }
}
