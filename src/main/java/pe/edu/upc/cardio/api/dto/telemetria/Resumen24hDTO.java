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
public class Resumen24hDTO {
    private List<PuntoGraficoDTO> puntos;
    private Boolean suficientesDatos;
    private String mensaje;
    private Double promedioPulso;
    private Integer maxPulso;
    private Integer minPulso;
}
