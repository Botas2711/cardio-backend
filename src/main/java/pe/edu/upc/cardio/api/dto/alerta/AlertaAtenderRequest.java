package pe.edu.upc.cardio.api.dto.alerta;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlertaAtenderRequest {
    @NotBlank(message = "La observación médica es obligatoria para atender la alerta")
    private String observacionMedica;
}
