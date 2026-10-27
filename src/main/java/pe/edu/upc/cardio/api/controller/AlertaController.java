package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.dto.alerta.*;
import pe.edu.upc.cardio.api.service.AlertaService;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Alertas Clínicas", description = "Supervisión médica de eventos críticos, explicabilidad SHAP y despacho de auxilio (HU016, HU019, HU020)")
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping("/recientes")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Listar alertas críticas pendientes para banner emergente en tiempo real", description = "HU020")
    public ResponseEntity<List<AlertaResponseDTO>> listarRecientes() {
        return ResponseEntity.ok(alertaService.listarAlertasNoAtendidas());
    }

    @GetMapping("/contador")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Contador de alertas clínicas no atendidas para la barra de navegación", description = "HU020")
    public ResponseEntity<AlertaContadorDTO> obtenerContador() {
        return ResponseEntity.ok(alertaService.contarAlertasNoAtendidas());
    }

    @GetMapping("/{idAlerta}/explicacion")
    @PreAuthorize("hasAnyRole('CARDIOLOGO', 'PACIENTE')")
    @Operation(summary = "Consultar explicabilidad clínica basada en valores SHAP y factores determinantes", description = "HU016")
    public ResponseEntity<AlertaExplicacionDTO> obtenerExplicacion(@PathVariable("idAlerta") Long idAlerta) {
        return ResponseEntity.ok(alertaService.obtenerExplicacionShap(idAlerta));
    }

    @PutMapping("/{idAlerta}/atender")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Marcar alerta crítica como atendida con observación clínica", description = "HU020")
    public ResponseEntity<AlertaResponseDTO> atenderAlerta(
            @PathVariable("idAlerta") Long idAlerta,
            @Valid @RequestBody AlertaAtenderRequest request) {
        return ResponseEntity.ok(alertaService.atenderAlerta(idAlerta, request));
    }

    @GetMapping("/paciente/{idPaciente}")
    @PreAuthorize("hasAnyRole('CARDIOLOGO', 'PACIENTE')")
    @Operation(summary = "Listar historial de alertas de un paciente específico")
    public ResponseEntity<List<AlertaResponseDTO>> listarPorPaciente(@PathVariable("idPaciente") Long idPaciente) {
        return ResponseEntity.ok(alertaService.listarPorPaciente(idPaciente));
    }
}
