package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.fastapi.FastApiPredictRequest;
import pe.edu.upc.cardio.api.dto.fastapi.FastApiPredictResponse;
import pe.edu.upc.cardio.api.model.Paciente;

public interface FastApiClientService {
    FastApiPredictResponse predecirRiesgo(FastApiPredictRequest request, Paciente paciente);
}
