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
public class BloqueHorarioDTO {
    private String bloque;
    private String intervalo;
    private Integer totalRegistros;
    private Double promedioPulso;
    private String mensaje;
    private List<PuntoGraficoDTO> lecturas;
}
