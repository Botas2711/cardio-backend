package pe.edu.upc.cardio.api.dto.proceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.cardio.api.model.EstadoConformidad;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcesoDTO {
    private Long idEvento;
    private Long idPaciente;
    private String nombrePaciente;
    private String dniPaciente;
    private String actividad;
    private OffsetDateTime fechaInicio;
    private OffsetDateTime fechaFin;
    private String ejecutor;
    private EstadoConformidad estadoConformidad;
}
