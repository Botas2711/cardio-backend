package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.dto.telemetria.*;
import pe.edu.upc.cardio.api.service.TelemetriaService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/telemetria")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Telemetría y Bioseñales", description = "Ingesta telemétrica PPG, curvas en vivo y resúmenes históricos (HU005 - HU011, HU014, HU015)")
public class TelemetriaController {

    private final TelemetriaService telemetriaService;

    @PostMapping("/ingesta")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Ingesta telemétrica continua desde el smartwatch/móvil", description = "HU005, HU006, HU007")
    public ResponseEntity<TelemetriaResponseDTO> ingesta(@Valid @RequestBody TelemetriaIngestRequest request) {
        TelemetriaResponseDTO res = telemetriaService.procesarIngesta(request);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/sincronizar")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Sincronización por lotes tras pérdida de enlace a internet", description = "HU008")
    public ResponseEntity<List<TelemetriaResponseDTO>> sincronizarLote(@Valid @RequestBody TelemetriaBatchRequest request) {
        return ResponseEntity.ok(telemetriaService.procesarLote(request));
    }

    @GetMapping("/paciente/{idPaciente}/en-vivo")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Consultar signos vitales actuales en tiempo real", description = "HU009 & HU014")
    public ResponseEntity<TelemetriaEnVivoDTO> obtenerEnVivo(@PathVariable("idPaciente") Long idPaciente) {
        return ResponseEntity.ok(telemetriaService.obtenerEnVivo(idPaciente));
    }

    @GetMapping("/paciente/{idPaciente}/resumen-24h")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Gráfico resumen con variaciones fisiológicas de las últimas 24 horas", description = "HU010")
    public ResponseEntity<Resumen24hDTO> obtenerResumen24h(@PathVariable("idPaciente") Long idPaciente) {
        return ResponseEntity.ok(telemetriaService.obtenerResumen24h(idPaciente));
    }

    @GetMapping("/paciente/{idPaciente}/bloque-horario")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Filtrar registros por bloque horario (MAÑANA, TARDE, NOCHE, MADRUGADA)", description = "HU011")
    public ResponseEntity<BloqueHorarioDTO> obtenerPorBloqueHorario(
            @PathVariable("idPaciente") Long idPaciente,
            @RequestParam(name = "bloque", defaultValue = "MAÑANA") String bloque,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(telemetriaService.obtenerPorBloqueHorario(idPaciente, bloque, fecha));
    }

    @GetMapping("/paciente/{idPaciente}/historico")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Curvas históricas de pulso y presión con promedios, máximos y mínimos", description = "HU015")
    public ResponseEntity<HistoricoResumenDTO> obtenerHistorico(
            @PathVariable("idPaciente") Long idPaciente,
            @RequestParam(name = "agrupacion", defaultValue = "DIARIO") String agrupacion,
            @RequestParam(name = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(name = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        return ResponseEntity.ok(telemetriaService.obtenerHistorico(idPaciente, agrupacion, inicio, fin));
    }
}
