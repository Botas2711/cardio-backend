package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.alerta.*;
import pe.edu.upc.cardio.api.dto.recomendacion.RecomendacionesResponseDTO;

import java.util.List;

public interface AlertaService {
    List<AlertaResponseDTO> listarAlertasNoAtendidas();
    AlertaContadorDTO contarAlertasNoAtendidas();
    AlertaResponseDTO atenderAlerta(Long idAlerta, AlertaAtenderRequest request);
    AlertaExplicacionDTO obtenerExplicacionShap(Long idAlerta);
    RecomendacionesResponseDTO obtenerRecomendacionesPaciente(Long idPaciente);
    List<AlertaResponseDTO> listarPorPaciente(Long idPaciente);
}
