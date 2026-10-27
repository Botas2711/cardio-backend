package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.auth.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    MessageResponse solicitarRecuperacionClave(ForgotPasswordRequest request);
    MessageResponse restablecerClave(ResetPasswordRequest request);
}
