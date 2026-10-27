package pe.edu.upc.cardio.api.dto.contacto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactoAuxilioResponseDTO {
    private Long idContacto;
    private Long idPaciente;
    private String nombreCompleto;
    private String telefono;
    private String relacion;
}
