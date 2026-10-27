package pe.edu.upc.cardio.api.dto.contacto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ContactoAuxilioRequest {

    @NotBlank(message = "El nombre completo del contacto es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El número telefónico es obligatorio")
    @Pattern(regexp = "^9\\d{8}$", message = "Número inválido. Ingrese un número telefónico válido de 9 dígitos")
    private String telefono;

    private String relacion;
}
