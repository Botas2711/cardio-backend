package pe.edu.upc.cardio.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.cardio.api.service.ReporteClinicoService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes Clínicos", description = "Descarga de resúmenes médicos consolidados en PDF (HU017)")
public class ReporteController {

    private final ReporteClinicoService reporteService;

    @GetMapping("/paciente/{idPaciente}/pdf")
    @PreAuthorize("hasAnyRole('CARDIOLOGO', 'PACIENTE')")
    @Operation(summary = "Descargar informe clínico consolidado del paciente en formato PDF", description = "HU017")
    public ResponseEntity<byte[]> descargarReportePdf(
            @PathVariable("idPaciente") Long idPaciente,
            @RequestParam(name = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(name = "medico", required = false) String medico) {

        byte[] pdfBytes = reporteService.generarReportePdf(idPaciente, fechaInicio, fechaFin, medico);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = String.format("informe_clinico_paciente_%d_%s.pdf", idPaciente, LocalDate.now());
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
