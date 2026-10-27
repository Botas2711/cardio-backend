package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.triage.PacienteTriageDTO;
import pe.edu.upc.cardio.api.model.NivelRiesgo;

import java.util.List;

public interface TriageService {
    List<PacienteTriageDTO> obtenerTriage(String query, NivelRiesgo nivelRiesgo);
}
