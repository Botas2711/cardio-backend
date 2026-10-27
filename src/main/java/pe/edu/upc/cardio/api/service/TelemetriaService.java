package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.telemetria.*;

import java.time.LocalDate;
import java.util.List;

public interface TelemetriaService {
    TelemetriaResponseDTO procesarIngesta(TelemetriaIngestRequest request);
    List<TelemetriaResponseDTO> procesarLote(TelemetriaBatchRequest request);
    TelemetriaEnVivoDTO obtenerEnVivo(Long idPaciente);
    Resumen24hDTO obtenerResumen24h(Long idPaciente);
    BloqueHorarioDTO obtenerPorBloqueHorario(Long idPaciente, String bloque, LocalDate fecha);
    HistoricoResumenDTO obtenerHistorico(Long idPaciente, String agrupacion, LocalDate inicio, LocalDate fin);
}
