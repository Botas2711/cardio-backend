package pe.edu.upc.cardio.api.dto.alerta;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaContadorDTO {
    private long totalNoAtendidas;
}
