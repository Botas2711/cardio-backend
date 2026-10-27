package pe.edu.upc.cardio.api.dto.alerta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.cardio.api.model.TipoAlerta;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaResponseDTO {
    private Long idAlerta;
    private Long idPaciente;
    private String nombrePaciente;
    private String dniPaciente;
    private OffsetDateTime fechaHora;
    private TipoAlerta tipoAlerta;
    private String valorRegistrado;
    private String explicacionShap;
    private Boolean notificacionEnviada;
    private Boolean atendida;
    private String observacionMedica;
}
