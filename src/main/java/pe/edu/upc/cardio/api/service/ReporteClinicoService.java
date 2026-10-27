package pe.edu.upc.cardio.api.service;

import java.time.LocalDate;

public interface ReporteClinicoService {
    byte[] generarReportePdf(Long idPaciente, LocalDate fechaInicio, LocalDate fechaFin);
    byte[] generarReportePdf(Long idPaciente, LocalDate fechaInicio, LocalDate fechaFin, String medico);
}
