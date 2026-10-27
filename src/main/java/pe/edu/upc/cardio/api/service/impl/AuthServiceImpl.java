package pe.edu.upc.cardio.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.dto.auth.*;
import pe.edu.upc.cardio.api.exception.BadRequestException;
import pe.edu.upc.cardio.api.exception.ResourceNotFoundException;
import pe.edu.upc.cardio.api.model.Paciente;
import pe.edu.upc.cardio.api.model.Rol;
import pe.edu.upc.cardio.api.model.Usuario;
import pe.edu.upc.cardio.api.repository.PacienteRepository;
import pe.edu.upc.cardio.api.repository.UsuarioRepository;
import pe.edu.upc.cardio.api.security.JwtTokenProvider;
import pe.edu.upc.cardio.api.service.AuthService;
import pe.edu.upc.cardio.api.service.EmailService;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(request.getCorreo().trim());

        if (usuarioOpt.isEmpty()) {
            // HU002 Escenario 3: Paciente no registrado
            throw new ResourceNotFoundException("El usuario no se encuentra registrado en el sistema");
        }

        Usuario usuario = usuarioOpt.get();

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new BadRequestException("La cuenta de usuario se encuentra inactiva");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo().trim(), request.getPassword())
            );

            String token = tokenProvider.generateToken(authentication, usuario.getIdUsuario(), usuario.getRol().name());

            AuthResponse.AuthResponseBuilder builder = AuthResponse.builder()
                    .token(token)
                    .tipoToken("Bearer")
                    .idUsuario(usuario.getIdUsuario())
                    .correo(usuario.getCorreo())
                    .rol(usuario.getRol());

            if (usuario.getRol() == Rol.PACIENTE) {
                pacienteRepository.findByUsuarioIdUsuario(usuario.getIdUsuario()).ifPresent(paciente -> {
                    builder.idPaciente(paciente.getIdPaciente())
                            .nombres(paciente.getNombres())
                            .apellidos(paciente.getApellidos())
                            .dni(paciente.getDni());
                });
            } else {
                if ("agustinalex14@gmail.com".equalsIgnoreCase(usuario.getCorreo())) {
                    builder.nombres("Agustín");
                    builder.apellidos("Aguilar");
                } else {
                    builder.nombres("Pablo");
                    builder.apellidos("Rojas");
                }
            }

            return builder.build();

        } catch (BadCredentialsException ex) {
            // HU001 Escenario 2 / HU002 Escenario 2
            throw new BadCredentialsException("Credenciales incorrectas. Verifique su correo o contraseña.");
        }
    }

    @Override
    @Transactional
    public MessageResponse solicitarRecuperacionClave(ForgotPasswordRequest request) {
        // HU004 Escenario 2: Correo no registrado
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo().trim())
                .orElseThrow(() -> new ResourceNotFoundException("No existe una cuenta asociada al correo proporcionado"));

        // HU004 Escenario 1: Generación de nueva contraseña provisional y token de recuperación
        String resetToken = UUID.randomUUID().toString();
        String nuevaClaveProvisional = "Cardio" + (1000 + new SecureRandom().nextInt(9000)) + "@";

        usuario.setClaveHash(passwordEncoder.encode(nuevaClaveProvisional));
        usuario.setTokenRecuperacion(resetToken);
        usuario.setTokenExpiracion(OffsetDateTime.now().plusHours(1));
        usuarioRepository.save(usuario);

        String nombreCompleto = "Usuario";
        if (usuario.getRol() == Rol.PACIENTE) {
            nombreCompleto = pacienteRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                    .map(p -> p.getNombres() + " " + p.getApellidos())
                    .orElse("Estimado Paciente");
        } else if (usuario.getRol() == Rol.CARDIOLOGO) {
            nombreCompleto = usuario.getCorreo().contains("agustin") ? "Dr. Agustín Aguilar" : "Dr. Cardiólogo";
        }

        emailService.enviarCorreoRecuperacion(usuario.getCorreo(), nombreCompleto, nuevaClaveProvisional);

        return MessageResponse.builder()
                .exitoso(true)
                .mensaje("Se ha enviado un correo a " + usuario.getCorreo() + " con su nueva contraseña provisional de acceso.")
                .build();
    }

    @Override
    @Transactional
    public MessageResponse restablecerClave(ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByTokenRecuperacion(request.getToken())
                .orElseThrow(() -> new BadRequestException("El token de recuperación es inválido o ha caducado"));

        if (usuario.getTokenExpiracion() != null && usuario.getTokenExpiracion().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("El token de recuperación ha expirado");
        }

        usuario.setClaveHash(passwordEncoder.encode(request.getNuevaPassword()));
        usuario.setTokenRecuperacion(null);
        usuario.setTokenExpiracion(null);
        usuarioRepository.save(usuario);

        return MessageResponse.builder()
                .exitoso(true)
                .mensaje("Contraseña restablecida exitosamente. Ahora puede iniciar sesión con sus nuevas credenciales.")
                .build();
    }
}
