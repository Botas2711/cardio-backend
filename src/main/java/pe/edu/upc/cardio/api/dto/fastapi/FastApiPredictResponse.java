package pe.edu.upc.cardio.api.dto.fastapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiPredictResponse {

    @JsonProperty("nivel_riesgo")
    private String nivelRiesgo;

    @JsonProperty("score_riesgo")
    private Double scoreRiesgo;

    @JsonProperty("explicacion_shap")
    private String explicacionShap;

    @JsonProperty("es_ruido")
    private Boolean esRuido;
}
