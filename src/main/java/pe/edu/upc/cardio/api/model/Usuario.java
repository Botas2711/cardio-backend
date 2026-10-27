package pe.edu.upc.cardio.api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "correo", length = 150, unique = true, nullable = false)
    private String correo;

    @Column(name = "clave_hash", length = 255, nullable = false)
    private String claveHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 30, nullable = false)
    private Rol rol;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "token_recuperacion", length = 255)
    private String tokenRecuperacion;

    @Column(name = "token_expiracion")
    private OffsetDateTime tokenExpiracion;

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
    }
}
