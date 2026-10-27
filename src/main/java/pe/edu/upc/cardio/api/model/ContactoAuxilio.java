package pe.edu.upc.cardio.api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contacto_auxilio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactoAuxilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto")
    private Long idContacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @Column(name = "nombre_completo", length = 150, nullable = false)
    private String nombreCompleto;

    @Column(name = "telefono", length = 15, nullable = false)
    private String telefono;

    @Column(name = "relacion", length = 50)
    private String relacion;
}
