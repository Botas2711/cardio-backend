package pe.edu.upc.cardio.api.dto.recomendacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionesResponseDTO {
    private boolean tieneAlertasRecientes;
    private String mensaje;
    private List<RecomendacionDTO> recomendaciones;
}
