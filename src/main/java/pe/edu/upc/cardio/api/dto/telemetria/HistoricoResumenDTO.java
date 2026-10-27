package pe.edu.upc.cardio.api.dto.telemetria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoResumenDTO {
    private String agrupacion;
    private Integer totalRegistros;
    private Double promedioPulso;
    private Integer maxPulso;
    private Integer minPulso;
    private String mensaje;
    private List<PuntoGraficoDTO> mediciones;
}
