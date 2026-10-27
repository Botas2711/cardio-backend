package pe.edu.upc.cardio.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upc.cardio.api.model.Rol;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tipoToken = "Bearer";
    private Long idUsuario;
    private String correo;
    private Rol rol;
    private Long idPaciente;
    private String nombres;
    private String apellidos;
    private String dni;
}
