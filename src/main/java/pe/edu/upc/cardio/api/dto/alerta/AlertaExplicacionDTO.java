package pe.edu.upc.cardio.api.dto.alerta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.cardio.api.model.NivelRiesgo;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaExplicacionDTO {
    private Long idAlerta;
    private Long idPaciente;
    private String nombrePaciente;
    private NivelRiesgo nivelRiesgo;
    private Double scoreRiesgo;
    private String estadoEvaluacion;
    private String mensajeClinico;
    private List<FactorShapDTO> factores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FactorShapDTO {
        private String factor;
        private String valorRegistrado;
        private String limiteSeguro;
        private Double pesoShap;
        private String impacto;
        private String descripcion;
    }
}
