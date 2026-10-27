package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.dto.triage.PacienteTriageDTO;
import pe.edu.upc.cardio.api.model.NivelRiesgo;
import pe.edu.upc.cardio.api.service.TriageService;

import java.util.List;

@RestController
@RequestMapping("/api/triage")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Triage Clínico", description = "Panel web del Cardiólogo con estratificación de riesgo en tiempo real (HU013, HU014)")
public class TriageController {

    private final TriageService triageService;

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Lista general de pacientes priorizada por criticidad y estado de enlace", description = "HU013 & HU014")
    public ResponseEntity<List<PacienteTriageDTO>> obtenerTriage(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "nivelRiesgo", required = false) NivelRiesgo nivelRiesgo) {
        return ResponseEntity.ok(triageService.obtenerTriage(query, nivelRiesgo));
    }
}
