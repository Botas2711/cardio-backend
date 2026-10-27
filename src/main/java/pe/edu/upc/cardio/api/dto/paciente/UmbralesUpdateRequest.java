package pe.edu.upc.cardio.api.dto.paciente;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UmbralesUpdateRequest {

    @NotNull(message = "La frecuencia mínima es obligatoria")
    @Min(value = 30, message = "La frecuencia mínima debe ser al menos 30 ppm")
    @Max(value = 150, message = "La frecuencia mínima no puede exceder 150 ppm")
    private Integer frecuenciaMin;

    @NotNull(message = "La frecuencia máxima es obligatoria")
    @Min(value = 50, message = "La frecuencia máxima debe ser al menos 50 ppm")
    @Max(value = 220, message = "La frecuencia máxima no puede exceder 220 ppm")
    private Integer frecuenciaMax;
}
