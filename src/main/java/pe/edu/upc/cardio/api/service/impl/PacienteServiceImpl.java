package pe.edu.upc.cardio.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.dto.paciente.*;
import pe.edu.upc.cardio.api.exception.BadRequestException;
import pe.edu.upc.cardio.api.exception.ResourceNotFoundException;
import pe.edu.upc.cardio.api.model.Paciente;
import pe.edu.upc.cardio.api.model.Rol;
import pe.edu.upc.cardio.api.model.Usuario;
import pe.edu.upc.cardio.api.repository.PacienteRepository;
import pe.edu.upc.cardio.api.repository.UsuarioRepository;
import pe.edu.upc.cardio.api.service.PacienteService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final pe.edu.upc.cardio.api.service.EmailService emailService;

    @Override
    @Transactional
    public PacienteResponseDTO registrarPaciente(PacienteCreateRequest request) {
        // HU028 Escenario 2: Intento de registro con documento duplicado
        if (pacienteRepository.existsByDni(request.getDni().trim())) {
            throw new BadRequestException("El documento de identidad ya se encuentra registrado en el sistema");
        }

        // Validación de correo duplicado
        if (usuarioRepository.existsByCorreo(request.getCorreo().trim())) {
            throw new BadRequestException("El correo electrónico ya se encuentra registrado en el sistema");
        }

        // Validación de umbrales iniciales
        if (request.getFrecuenciaMax() <= request.getFrecuenciaMin()) {
            throw new BadRequestException("Rango inválido. El valor máximo de frecuencia debe ser superior al mínimo");
        }

        // Crear credencial de usuario móvil: contraseña por defecto <dni>@Cardio
        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : request.getDni().trim() + "@Cardio";

        Usuario usuario = Usuario.builder()
                .correo(request.getCorreo().trim().toLowerCase())
                .claveHash(passwordEncoder.encode(rawPassword))
                .rol(Rol.PACIENTE)
                .activo(true)
                .build();
        usuario = usuarioRepository.save(usuario);

        LocalDate fechaNacimiento = request.getFechaNacimiento();
        int edad = 0;
        if (fechaNacimiento != null) {
            edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        } else if (request.getEdad() != null && request.getEdad() > 0) {
            edad = request.getEdad();
            fechaNacimiento = LocalDate.now().minusYears(edad).withDayOfYear(1);
        } else {
            fechaNacimiento = LocalDate.of(1985, 1, 1);
            edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        }

        BigDecimal imc = BigDecimal.ZERO;
        if (request.getPesoKg() != null && request.getTallaCm() != null && request.getTallaCm() > 0) {
            BigDecimal tallaM = BigDecimal.valueOf(request.getTallaCm()).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal tallaCuadrado = tallaM.multiply(tallaM);
            imc = request.getPesoKg().divide(tallaCuadrado, 2, RoundingMode.HALF_UP);
        }

        boolean obesidadCalculada = Boolean.TRUE.equals(request.getObesidad()) || (imc.compareTo(new BigDecimal("30.0")) >= 0);

        String genero = (request.getGenero() != null && !request.getGenero().isBlank())
                ? request.getGenero().trim().toUpperCase()
                : "MASCULINO";

        // Crear paciente
        Paciente paciente = Paciente.builder()
                .usuario(usuario)
                .dni(request.getDni().trim())
                .nombres(request.getNombres().trim())
                .apellidos(request.getApellidos().trim())
                .fechaNacimiento(fechaNacimiento)
                .edad(edad)
                .genero(genero)
                .pesoKg(request.getPesoKg() != null ? request.getPesoKg() : new BigDecimal("70.0"))
                .tallaCm(request.getTallaCm() != null ? request.getTallaCm() : 170)
                .imc(imc)
                .obesidad(obesidadCalculada)
                .tabaquismo(Boolean.TRUE.equals(request.getTabaquismo()))
                .consumoAlcohol(Boolean.TRUE.equals(request.getConsumoAlcohol()))
                .diabetes(Boolean.TRUE.equals(request.getDiabetes()))
                .antecedentesCv(Boolean.TRUE.equals(request.getAntecedentesCv()))
                .usoMedicacion(Boolean.TRUE.equals(request.getUsoMedicacion()))
                .hipertension(Boolean.TRUE.equals(request.getHipertension()))
                .tiroides(Boolean.TRUE.equals(request.getTiroides()))
                .acv(Boolean.TRUE.equals(request.getAcv()))
                .frecuenciaMin(request.getFrecuenciaMin() != null ? request.getFrecuenciaMin() : 60)
                .frecuenciaMax(request.getFrecuenciaMax() != null ? request.getFrecuenciaMax() : 100)
                .build();

        paciente = pacienteRepository.save(paciente);

        // Envío de credenciales para la app móvil a través de Mailtrap (correo y contraseña)
        emailService.enviarCredencialesPaciente(
                usuario.getCorreo(),
                paciente.getNombres() + " " + paciente.getApellidos(),
                usuario.getCorreo(),
                rawPassword
        );

        return mapearADto(paciente);
    }

    @Override
    @Transactional
    public PacienteResponseDTO actualizarPaciente(Long idPaciente, PacienteUpdateRequest request) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente));

        paciente.setNombres(request.getNombres().trim());
        paciente.setApellidos(request.getApellidos().trim());
        paciente.setFechaNacimiento(request.getFechaNacimiento());
        paciente.setGenero(request.getGenero().trim().toUpperCase());
        if (request.getPesoKg() != null) paciente.setPesoKg(request.getPesoKg());
        if (request.getTallaCm() != null) paciente.setTallaCm(request.getTallaCm());
        if (request.getDiabetes() != null) paciente.setDiabetes(request.getDiabetes());
        if (request.getAntecedentesCv() != null) paciente.setAntecedentesCv(request.getAntecedentesCv());
        if (request.getTabaquismo() != null) paciente.setTabaquismo(request.getTabaquismo());
        if (request.getObesidad() != null) paciente.setObesidad(request.getObesidad());
        if (request.getConsumoAlcohol() != null) paciente.setConsumoAlcohol(request.getConsumoAlcohol());
        if (request.getUsoMedicacion() != null) paciente.setUsoMedicacion(request.getUsoMedicacion());
        if (request.getHipertension() != null) paciente.setHipertension(request.getHipertension());
        if (request.getTiroides() != null) paciente.setTiroides(request.getTiroides());
        if (request.getAcv() != null) paciente.setAcv(request.getAcv());

        paciente.calcularCamposDerivados();
        paciente = pacienteRepository.save(paciente);
        return mapearADto(paciente);
    }

    @Override
    @Transactional
    public PacienteResponseDTO actualizarUmbrales(Long idPaciente, UmbralesUpdateRequest request) {
        // HU027 Escenario 2: Ingreso de valores fuera de rango fisiológico
        if (request.getFrecuenciaMax() <= request.getFrecuenciaMin()) {
            throw new BadRequestException("Rango inválido. El valor máximo debe ser superior al mínimo");
        }

        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente));

        paciente.setFrecuenciaMin(request.getFrecuenciaMin());
        paciente.setFrecuenciaMax(request.getFrecuenciaMax());

        paciente = pacienteRepository.save(paciente);
        return mapearADto(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPorId(Long idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente));
        return mapearADto(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResponseDTO obtenerPorUsuarioId(Long idUsuario) {
        Paciente paciente = pacienteRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de paciente asociado al usuario"));
        return mapearADto(paciente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.findAll().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }
        return pacienteRepository.existsByDni(dni.trim());
    }

    private PacienteResponseDTO mapearADto(Paciente p) {
        int edad = (p.getFechaNacimiento() != null)
                ? Period.between(p.getFechaNacimiento(), LocalDate.now()).getYears()
                : (p.getEdad() != null ? p.getEdad() : 0);

        return PacienteResponseDTO.builder()
                .idPaciente(p.getIdPaciente())
                .idUsuario(p.getUsuario() != null ? p.getUsuario().getIdUsuario() : null)
                .correo(p.getUsuario() != null ? p.getUsuario().getCorreo() : null)
                .dni(p.getDni())
                .nombres(p.getNombres())
                .apellidos(p.getApellidos())
                .fechaNacimiento(p.getFechaNacimiento())
                .edad(edad)
                .genero(p.getGenero())
                .pesoKg(p.getPesoKg())
                .tallaCm(p.getTallaCm())
                .imc(p.getImc())
                .obesidad(p.getObesidad())
                .tabaquismo(p.getTabaquismo())
                .consumoAlcohol(p.getConsumoAlcohol())
                .diabetes(p.getDiabetes())
                .antecedentesCv(p.getAntecedentesCv())
                .usoMedicacion(p.getUsoMedicacion())
                .hipertension(p.getHipertension())
                .tiroides(p.getTiroides())
                .acv(p.getAcv())
                .frecuenciaMin(p.getFrecuenciaMin())
                .frecuenciaMax(p.getFrecuenciaMax())
                .build();
    }
}
