package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.proceso.*;
import pe.edu.upc.cardio.api.model.NivelRiesgo;

import java.util.List;

public interface EventoProcesoService {
    EventoProcesoDTO registrarEvento(EventoProcesoRequest request);
    List<EventoProcesoDTO> listarEventos(NivelRiesgo filtroRiesgo);
    MapaProcesoDTO obtenerMapaProceso(NivelRiesgo filtroRiesgo);
    TiemposProcesoDTO obtenerTiemposProceso(NivelRiesgo filtroRiesgo);
    ReporteConformidadDTO obtenerReporteConformidad(NivelRiesgo filtroRiesgo);
    FlujoTelemetriaDTO obtenerFlujoTelemetria(boolean riesgoAlto);
}
