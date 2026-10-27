package pe.edu.upc.cardio.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "alerta_clinica", indexes = {
        @Index(name = "idx_alerta_paciente", columnList = "id_paciente"),
        @Index(name = "idx_alerta_fecha_hora", columnList = "fecha_hora"),
        @Index(name = "idx_alerta_atendida", columnList = "atendida")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_telemetria", nullable = false)
    private TelemetriaSenal telemetria;

    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_alerta", length = 50, nullable = false)
    private TipoAlerta tipoAlerta;

    @Column(name = "valor_registrado", length = 100, nullable = false)
    private String valorRegistrado;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "explicacion_shap", columnDefinition = "jsonb")
    private String explicacionShap;

    @Builder.Default
    @Column(name = "notificacion_enviada", nullable = false)
    private Boolean notificacionEnviada = false;

    @Builder.Default
    @Column(name = "atendida", nullable = false)
    private Boolean atendida = false;

    @Column(name = "observacion_medica", length = 500)
    private String observacionMedica;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = OffsetDateTime.now();
        }
        if (notificacionEnviada == null) {
            notificacionEnviada = false;
        }
        if (atendida == null) {
            atendida = false;
        }
    }
}
