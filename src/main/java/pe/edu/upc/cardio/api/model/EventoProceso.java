package pe.edu.upc.cardio.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "evento_proceso", indexes = {
        @Index(name = "idx_evento_paciente", columnList = "id_paciente"),
        @Index(name = "idx_evento_fecha_inicio", columnList = "fecha_inicio")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoProceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(name = "actividad", length = 100, nullable = false)
    private String actividad;

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;

    @Column(name = "ejecutor", length = 100)
    private String ejecutor;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "estado_conformidad", length = 30, nullable = false)
    private EstadoConformidad estadoConformidad = EstadoConformidad.CONFORME;

    @PrePersist
    public void prePersist() {
        if (fechaInicio == null) {
            fechaInicio = OffsetDateTime.now();
        }
        if (estadoConformidad == null) {
            estadoConformidad = EstadoConformidad.CONFORME;
        }
    }
}
