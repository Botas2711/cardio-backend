package pe.edu.upc.cardio.api.dto.telemetria;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class TelemetriaIngestRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long idPaciente;

    private OffsetDateTime fechaHora;

    @NotNull(message = "El pulso en PPM es obligatorio")
    private Integer pulsoPpm;

    private String senalPpgCruda;

    private Boolean artefactoRuido = false;

    private Boolean contactoDermal = true;

    private String dispositivoNombre;

    private Integer bateriaPorcentaje;

    private String macDispositivo;

    private Integer spo2Porcentaje;
}
