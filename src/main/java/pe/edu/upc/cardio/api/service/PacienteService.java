package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.paciente.*;

import java.util.List;

public interface PacienteService {
    PacienteResponseDTO registrarPaciente(PacienteCreateRequest request);
    PacienteResponseDTO actualizarPaciente(Long idPaciente, PacienteUpdateRequest request);
    PacienteResponseDTO actualizarUmbrales(Long idPaciente, UmbralesUpdateRequest request);
    PacienteResponseDTO obtenerPorId(Long idPaciente);
    PacienteResponseDTO obtenerPorUsuarioId(Long idUsuario);
    List<PacienteResponseDTO> listarTodos();
    boolean existeDni(String dni);
}
