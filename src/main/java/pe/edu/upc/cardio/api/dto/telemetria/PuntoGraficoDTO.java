package pe.edu.upc.cardio.api.dto.telemetria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.cardio.api.model.NivelRiesgo;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoGraficoDTO {
    private OffsetDateTime fechaHora;
    private Integer pulsoPpm;
    private NivelRiesgo nivelRiesgo;
    private java.math.BigDecimal scoreRiesgo;
}
