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
public class MapaProcesoDTO {
    private int totalCasos;
    private List<NodoActividadDTO> actividades;
    private List<AristaTransicionDTO> transiciones;
    private String mensaje;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NodoActividadDTO {
        private String actividad;
        private long cantidadPacientes;
        private long totalOcurrencias;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AristaTransicionDTO {
        private String origen;
        private String destino;
        private long cantidadCasos;
    }
}
