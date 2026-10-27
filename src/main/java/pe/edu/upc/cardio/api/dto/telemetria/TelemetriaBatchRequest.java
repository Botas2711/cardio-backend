package pe.edu.upc.cardio.api.dto.telemetria;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TelemetriaBatchRequest {

    @NotEmpty(message = "El lote de telemetría no puede estar vacío")
    @Valid
    private List<TelemetriaIngestRequest> mediciones;
}
