package pe.edu.upc.cardio.api.dto.fastapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FastApiPredictRequest {

    @JsonProperty("heart_rate")
    private Integer pulsoPpm;

    @JsonProperty("age")
    private Integer edad;

    @JsonProperty("sex")
    private Integer sexo; // 1: Masculino, 0: Femenino

    @JsonProperty("bmi")
    private BigDecimal imc;

    @JsonProperty("diabetes")
    private Boolean diabetes;

    @JsonProperty("family_history")
    private Boolean antecedentesCv;

    @JsonProperty("smoking")
    private Boolean tabaquismo;

    @JsonProperty("obesity")
    private Boolean obesidad;

    @JsonProperty("alcohol_consumption")
    private Boolean consumoAlcohol;

    @JsonProperty("medication_use")
    private Boolean usoMedicacion;

    @JsonProperty("hipertension")
    private Boolean hipertension;

    @JsonProperty("tiroides")
    private Boolean tiroides;

    @JsonProperty("acv")
    private Boolean acv;

    @JsonProperty("senal_ppg_cruda")
    private String senalPpgCruda;
}
