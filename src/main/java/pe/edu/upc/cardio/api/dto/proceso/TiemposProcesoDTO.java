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
public class TiemposProcesoDTO {
    private List<TransicionTiempoDTO> pasos;
    private String estadoGeneral;
    private String mensaje;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransicionTiempoDTO {
        private String etapaOrigen;
        private String etapaDestino;
        private double duracionPromedioMinutos;
        private double duracionLimiteNormalMinutos;
        private boolean demorado;
        private String colorAlerta; // "ROJO" si demorado, "VERDE" si dentro de lo normal
        private String mensajeEstado;
    }
}
