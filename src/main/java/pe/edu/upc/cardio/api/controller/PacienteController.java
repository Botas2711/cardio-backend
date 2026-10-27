package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.dto.paciente.*;
import pe.edu.upc.cardio.api.dto.recomendacion.RecomendacionesResponseDTO;
import pe.edu.upc.cardio.api.security.UserPrincipal;
import pe.edu.upc.cardio.api.service.AlertaService;
import pe.edu.upc.cardio.api.service.PacienteService;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Pacientes", description = "Gestión clínica de pacientes, umbrales basales y recomendaciones (HU012, HU027, HU028)")
public class PacienteController {

    private final PacienteService pacienteService;
    private final AlertaService alertaService;

    @PostMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Alta de nuevo paciente y generación de credenciales móviles", description = "HU028")
    public ResponseEntity<PacienteResponseDTO> registrarPaciente(@Valid @RequestBody PacienteCreateRequest request) {
        PacienteResponseDTO nuevo = pacienteService.registrarPaciente(request);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Listar todos los pacientes registrados", description = "HU013")
    public ResponseEntity<List<PacienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Obtener detalle clínico de un paciente por su ID")
    public ResponseEntity<PacienteResponseDTO> obtenerPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    @Operation(summary = "Obtener perfil del paciente actualmente autenticado")
    public ResponseEntity<PacienteResponseDTO> obtenerMiPerfil(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(pacienteService.obtenerPorUsuarioId(principal.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Actualizar datos demográficos del paciente")
    public ResponseEntity<PacienteResponseDTO> actualizarPaciente(
            @PathVariable("id") Long id,
            @Valid @RequestBody PacienteUpdateRequest request) {
        return ResponseEntity.ok(pacienteService.actualizarPaciente(id, request));
    }

    @PutMapping("/{id}/umbrales")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Personalizar límites y umbrales basales del paciente", description = "HU027")
    public ResponseEntity<PacienteResponseDTO> actualizarUmbrales(
            @PathVariable("id") Long id,
            @Valid @RequestBody UmbralesUpdateRequest request) {
        return ResponseEntity.ok(pacienteService.actualizarUmbrales(id, request));
    }

    @GetMapping("/{id}/recomendaciones")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Catálogo de recomendaciones preventivas e historial de advertencias (30 días)", description = "HU012")
    public ResponseEntity<RecomendacionesResponseDTO> obtenerRecomendaciones(@PathVariable("id") Long id) {
        return ResponseEntity.ok(alertaService.obtenerRecomendacionesPaciente(id));
    }

    @GetMapping("/verificar-dni/{dni}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Verificar si un DNI ya se encuentra registrado", description = "HU028")
    public ResponseEntity<java.util.Map<String, Boolean>> verificarDni(@PathVariable("dni") String dni) {
        boolean existe = pacienteService.existeDni(dni);
        return ResponseEntity.ok(java.util.Map.of("existe", existe));
    }
}
