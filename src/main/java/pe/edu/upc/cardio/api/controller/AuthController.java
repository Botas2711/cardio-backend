package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.dto.auth.*;
import pe.edu.upc.cardio.api.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para inicio de sesión, recuperación de contraseña y cierre de sesión (HU001 - HU004)")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión para Cardiólogos (Web) y Pacientes (App Móvil)", description = "HU001 & HU002")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/recuperar-clave")
    @Operation(summary = "Solicitar enlace temporal de recuperación de contraseña", description = "HU004")
    public ResponseEntity<MessageResponse> recuperarClave(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.solicitarRecuperacionClave(request));
    }

    @PostMapping("/restablecer-clave")
    @Operation(summary = "Restablecer contraseña usando token temporal", description = "HU004")
    public ResponseEntity<MessageResponse> restablecerClave(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.restablecerClave(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión activa", description = "HU003")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(MessageResponse.builder()
                .exitoso(true)
                .mensaje("Sesión cerrada exitosamente en el servidor")
                .build());
    }
}
