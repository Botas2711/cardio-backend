package pe.edu.upc.cardio.api.dto.proceso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlujoTelemetriaDTO {

    private String titulo;
    private String subtitulo;
    private boolean filtroRiesgoAlto;

    private String kpi1Titulo;
    private String kpi1Valor;

    private String kpi2Titulo;
    private String kpi2Valor;
    private String kpi2Color;

    private List<NodoTelemetriaDTO> nodos;
    private List<TransicionTelemetriaDTO> transiciones;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NodoTelemetriaDTO {
        private int paso;
        private String idActividad;
        private String titulo;
        private String subtitulo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransicionTelemetriaDTO {
        private String origen;
        private String destino;
        private long duracionMs;
        private String tiempoTexto;
        private String etiqueta;
        private String estadoConformidad;
        private String color;
        private boolean esCritico;
    }
}
