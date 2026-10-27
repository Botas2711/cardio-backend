package pe.edu.upc.cardio.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "paciente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long idPaciente;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", referencedColumnName = "id_usuario", unique = true, nullable = false)
    private Usuario usuario;

    @Column(name = "dni", length = 8, unique = true, nullable = false)
    private String dni;

    @Column(name = "nombres", length = 100, nullable = false)
    private String nombres;

    @Column(name = "apellidos", length = 100, nullable = false)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "genero", length = 15, nullable = false)
    private String genero;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "peso_kg", precision = 5, scale = 2, nullable = false)
    private BigDecimal pesoKg;

    @Column(name = "talla_cm", nullable = false)
    private Integer tallaCm;

    @Column(name = "imc", precision = 4, scale = 2, nullable = false)
    private BigDecimal imc;

    @Builder.Default
    @Column(name = "obesidad", nullable = false)
    private Boolean obesidad = false;

    @Builder.Default
    @Column(name = "tabaquismo", nullable = false)
    private Boolean tabaquismo = false;

    @Builder.Default
    @Column(name = "consumo_alcohol", nullable = false)
    private Boolean consumoAlcohol = false;

    @Builder.Default
    @Column(name = "diabetes", nullable = false)
    private Boolean diabetes = false;

    @Builder.Default
    @Column(name = "antecedentes_cv", nullable = false)
    private Boolean antecedentesCv = false;

    @Builder.Default
    @Column(name = "uso_medicacion", nullable = false)
    private Boolean usoMedicacion = false;

    @Builder.Default
    @Column(name = "hipertension", nullable = false)
    private Boolean hipertension = false;

    @Builder.Default
    @Column(name = "tiroides", nullable = false)
    private Boolean tiroides = false;

    @Builder.Default
    @Column(name = "acv", nullable = false)
    private Boolean acv = false;

    @Builder.Default
    @Column(name = "frecuencia_min", nullable = false)
    private Integer frecuenciaMin = 60;

    @Builder.Default
    @Column(name = "frecuencia_max", nullable = false)
    private Integer frecuenciaMax = 100;

    @PrePersist
    @PreUpdate
    public void calcularCamposDerivados() {
        if (fechaNacimiento != null) {
            edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        }
        if (pesoKg != null && tallaCm != null && tallaCm > 0) {
            BigDecimal tallaMetros = BigDecimal.valueOf(tallaCm).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal tallaCuadrado = tallaMetros.multiply(tallaMetros);
            imc = pesoKg.divide(tallaCuadrado, 2, RoundingMode.HALF_UP);
            if (imc.compareTo(new BigDecimal("30.0")) >= 0) {
                obesidad = true;
            }
        }
    }
}
