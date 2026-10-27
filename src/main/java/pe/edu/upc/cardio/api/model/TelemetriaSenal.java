package pe.edu.upc.cardio.api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "telemetria_senales", indexes = {
        @Index(name = "idx_telemetria_paciente", columnList = "id_paciente"),
        @Index(name = "idx_telemetria_fecha_hora", columnList = "fecha_hora")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetriaSenal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_telemetria")
    private Long idTelemetria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    @Column(name = "pulso_ppm", nullable = false)
    private Integer pulsoPpm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "senal_ppg_cruda", columnDefinition = "jsonb")
    private String senalPpgCruda;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_riesgo", length = 20, nullable = false)
    private NivelRiesgo nivelRiesgo;

    @Column(name = "score_riesgo", precision = 5, scale = 4)
    private BigDecimal scoreRiesgo;

    @Builder.Default
    @Column(name = "artefacto_ruido", nullable = false)
    private Boolean artefactoRuido = false;

    @Builder.Default
    @Column(name = "contacto_dermal", nullable = false)
    private Boolean contactoDermal = true;

    @Column(name = "dispositivo_nombre", length = 100)
    private String dispositivoNombre;

    @Column(name = "bateria_porcentaje")
    private Integer bateriaPorcentaje;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = OffsetDateTime.now();
        }
        if (artefactoRuido == null) {
            artefactoRuido = false;
        }
        if (contactoDermal == null) {
            contactoDermal = true;
        }
    }
}
