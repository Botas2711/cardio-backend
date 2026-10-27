package pe.edu.upc.cardio.api.dto.proceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteConformidadDTO {
    private double porcentajeCumplimiento;
    private long totalCasos;
    private long casosConformes;
    private long casosConDesvios;
    private String mensaje;
    private List<CasoDesvioDTO> casosIncompletos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CasoDesvioDTO {
        private Long idPaciente;
        private String nombrePaciente;
        private String dniPaciente;
        private String motivoDesvio;
    }
}
