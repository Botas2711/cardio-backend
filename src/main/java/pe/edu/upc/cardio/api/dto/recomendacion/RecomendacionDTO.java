package pe.edu.upc.cardio.api.dto.recomendacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecomendacionDTO {
    private OffsetDateTime fechaHora;
    private String tipoAlerta;
    private String valorRegistrado;
    private String pautaAutocuidado;
}
