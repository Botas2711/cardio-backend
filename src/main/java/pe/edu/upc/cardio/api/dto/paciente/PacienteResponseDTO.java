package pe.edu.upc.cardio.api.dto.paciente;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResponseDTO {
    private Long idPaciente;
    private Long idUsuario;
    private String correo;
    private String dni;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private Integer edad;
    private String genero;
    private BigDecimal pesoKg;
    private Integer tallaCm;
    private BigDecimal imc;
    private Boolean obesidad;
    private Boolean tabaquismo;
    private Boolean consumoAlcohol;
    private Boolean diabetes;
    private Boolean antecedentesCv;
    private Boolean usoMedicacion;
    private Boolean hipertension;
    private Boolean tiroides;
    private Boolean acv;
    private Integer frecuenciaMin;
    private Integer frecuenciaMax;
}
