package pe.edu.upc.cardio.api.dto.paciente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PacienteUpdateRequest {

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El género es obligatorio")
    private String genero;

    private BigDecimal pesoKg;
    private Integer tallaCm;
    private Boolean diabetes;
    private Boolean antecedentesCv;
    private Boolean tabaquismo;
    private Boolean obesidad;
    private Boolean consumoAlcohol;
    private Boolean usoMedicacion;
    private Boolean hipertension;
    private Boolean tiroides;
    private Boolean acv;
}
