package pe.edu.upc.cardio.api.dto.proceso;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.edu.upc.cardio.api.model.EstadoConformidad;

import java.time.OffsetDateTime;

@Data
public class EventoProcesoRequest {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long idPaciente;

    @NotBlank(message = "La actividad asistencial es obligatoria")
    private String actividad;

    private OffsetDateTime fechaInicio;

    private OffsetDateTime fechaFin;

    private String ejecutor;

    private EstadoConformidad estadoConformidad = EstadoConformidad.CONFORME;
}
