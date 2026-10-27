package pe.edu.upc.cardio.api.dto.paciente;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PacienteCreateRequest {

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    private Integer edad;

    private String celular;

    private String genero = "MASCULINO";

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe tener un formato válido")
    private String correo;

    private String password;

    @NotNull(message = "El peso en kg es obligatorio")
    @DecimalMin(value = "20.0", message = "El peso debe ser al menos 20 kg")
    @DecimalMax(value = "300.0", message = "El peso no puede exceder 300 kg")
    private BigDecimal pesoKg;

    @NotNull(message = "La talla en centímetros es obligatoria")
    @Min(value = 50, message = "La talla debe ser al menos 50 cm")
    @Max(value = 250, message = "La talla no puede exceder 250 cm")
    private Integer tallaCm;

    private Boolean diabetes = false;
    private Boolean antecedentesCv = false;
    private Boolean tabaquismo = false;
    private Boolean obesidad = false;
    private Boolean consumoAlcohol = false;
    private Boolean usoMedicacion = false;
    private Boolean hipertension = false;
    private Boolean tiroides = false;
    private Boolean acv = false;

    @Min(value = 30, message = "La frecuencia mínima debe ser al menos 30 ppm")
    @Max(value = 150, message = "La frecuencia mínima no puede exceder 150 ppm")
    private Integer frecuenciaMin = 60;

    @Min(value = 60, message = "La frecuencia máxima debe ser al menos 60 ppm")
    @Max(value = 220, message = "La frecuencia máxima no puede exceder 220 ppm")
    private Integer frecuenciaMax = 100;
}
