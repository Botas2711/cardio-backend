package pe.edu.upc.cardio.api.dto.telemetria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.cardio.api.model.NivelRiesgo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetriaResponseDTO {
    private Long idTelemetria;
    private Long idPaciente;
    private OffsetDateTime fechaHora;
    private Integer pulsoPpm;
    private NivelRiesgo nivelRiesgo;
    private BigDecimal scoreRiesgo;
    private Boolean artefactoRuido;
    private Boolean contactoDermal;
    private String senalPpgCruda;
    private String dispositivoNombre;
    private Integer bateriaPorcentaje;
}
