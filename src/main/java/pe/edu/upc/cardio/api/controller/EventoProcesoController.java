package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.dto.proceso.*;
import pe.edu.upc.cardio.api.model.NivelRiesgo;
import pe.edu.upc.cardio.api.service.EventoProcesoService;

import java.util.List;

@RestController
@RequestMapping("/api/procesos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Minería de Procesos (PM4Py)", description = "Auditoría asistencial, descubrimiento de rutas clínicas y cuellos de botella (HU021 - HU024)")
public class EventoProcesoController {

    private final EventoProcesoService eventoProcesoService;

    @PostMapping("/eventos")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Registrar evento asistencial hospitalario")
    public ResponseEntity<EventoProcesoDTO> registrarEvento(@Valid @RequestBody EventoProcesoRequest request) {
        EventoProcesoDTO nuevo = eventoProcesoService.registrarEvento(request);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @GetMapping("/eventos")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Listar log de eventos asistenciales")
    public ResponseEntity<List<EventoProcesoDTO>> listarEventos(@RequestParam(name = "filtroRiesgo", required = false) NivelRiesgo filtroRiesgo) {
        return ResponseEntity.ok(eventoProcesoService.listarEventos(filtroRiesgo));
    }

    @GetMapping("/mapa")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Visualizar mapa de flujo asistencial con volumen de pacientes", description = "HU021 & HU024")
    public ResponseEntity<MapaProcesoDTO> obtenerMapaProceso(@RequestParam(name = "filtroRiesgo", required = false) NivelRiesgo filtroRiesgo) {
        return ResponseEntity.ok(eventoProcesoService.obtenerMapaProceso(filtroRiesgo));
    }

    @GetMapping("/tiempos-espera")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Consultar tiempos promedio de espera y detección de cuellos de botella", description = "HU022 & HU024")
    public ResponseEntity<TiemposProcesoDTO> obtenerTiemposProceso(@RequestParam(name = "filtroRiesgo", required = false) NivelRiesgo filtroRiesgo) {
        return ResponseEntity.ok(eventoProcesoService.obtenerTiemposProceso(filtroRiesgo));
    }

    @GetMapping("/conformidad")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Auditar porcentaje de conformidad de la ruta clínica asistencial regular", description = "HU023 & HU024")
    public ResponseEntity<ReporteConformidadDTO> obtenerReporteConformidad(@RequestParam(name = "filtroRiesgo", required = false) NivelRiesgo filtroRiesgo) {
        return ResponseEntity.ok(eventoProcesoService.obtenerReporteConformidad(filtroRiesgo));
    }

    @GetMapping("/flujo-telemetria")
    @PreAuthorize("hasRole('CARDIOLOGO')")
    @Operation(summary = "Obtener mapa y métricas del flujo de telemetría digital", description = "Audita tiempos de procesamiento digital y respuesta humana desde EVENTO_PROCESO")
    public ResponseEntity<FlujoTelemetriaDTO> obtenerFlujoTelemetria(
            @RequestParam(name = "riesgoAlto", defaultValue = "false") boolean riesgoAlto
    ) {
        return ResponseEntity.ok(eventoProcesoService.obtenerFlujoTelemetria(riesgoAlto));
    }
}
