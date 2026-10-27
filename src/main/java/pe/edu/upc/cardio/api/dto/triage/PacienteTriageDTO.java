package pe.edu.upc.cardio.api.dto.triage;

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
public class PacienteTriageDTO {
    private Long idPaciente;
    private String dni;
    private String nombres;
    private String apellidos;
    private String genero;
    private Integer edad;
    private Integer ultimoPulso;
    private NivelRiesgo nivelRiesgoActual;
    private BigDecimal scoreRiesgo;
    private OffsetDateTime ultimaMedicionFechaHora;
    private Boolean enlaceActivo;
    private String estadoEnlaceTexto;
    private Long totalAlertasPendientes;
    private Integer frecuenciaMin;
    private Integer frecuenciaMax;
}
