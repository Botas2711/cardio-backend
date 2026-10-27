package pe.edu.upc.cardio.api.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.model.*;
import pe.edu.upc.cardio.api.repository.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final ContactoAuxilioRepository contactoRepository;
    private final TelemetriaSenalRepository telemetriaRepository;
    private final AlertaClinicaRepository alertaRepository;
    private final EventoProcesoRepository eventoRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) {
        asegurarCuentaCardiologo();
        asegurarPacientesMockup();
        asegurarCuentaPacienteApp();
        asegurarEventosTelemetriaProceso();

        if (pacienteRepository.count() > 0) {
            logger.info("Base de datos ya cuenta con registros clínicos previos.");
            return;
        }

        logger.info("Iniciando sembrado inicial de datos (Pacientes de prueba, Bioseñales y Procesos)...");

        // 2. Paciente 1: Riesgo Crítico (Juan Carlos Pérez)
        Usuario userJuan = Usuario.builder()
                .correo("juan.perez@gmail.com")
                .claveHash(passwordEncoder.encode("Paciente123!"))
                .rol(Rol.PACIENTE)
                .activo(true)
                .fechaCreacion(OffsetDateTime.now().minusDays(30))
                .build();
        usuarioRepository.save(userJuan);

        Paciente pacJuan = Paciente.builder()
                .usuario(userJuan)
                .dni("72849102")
                .nombres("Juan Carlos")
                .apellidos("Pérez Gómez")
                .fechaNacimiento(LocalDate.of(1964, 5, 12))
                .genero("MASCULINO")
                .edad(61)
                .pesoKg(new BigDecimal("88.5"))
                .tallaCm(168)
                .imc(new BigDecimal("31.4"))
                .obesidad(true)
                .tabaquismo(true)
                .consumoAlcohol(false)
                .diabetes(true)
                .antecedentesCv(true)
                .usoMedicacion(true)
                .hipertension(true)
                .tiroides(false)
                .acv(false)
                .frecuenciaMin(60)
                .frecuenciaMax(100)
                .build();
        pacJuan = pacienteRepository.save(pacJuan);

        ContactoAuxilio conJuan = ContactoAuxilio.builder()
                .paciente(pacJuan)
                .nombreCompleto("María Pérez")
                .telefono("987654321")
                .relacion("Hija")
                .build();
        contactoRepository.save(conJuan);

        TelemetriaSenal telJuanCritica = TelemetriaSenal.builder()
                .paciente(pacJuan)
                .fechaHora(OffsetDateTime.now().minusMinutes(2))
                .pulsoPpm(138)
                .nivelRiesgo(NivelRiesgo.CRITICO)
                .scoreRiesgo(new BigDecimal("0.9620"))
                .artefactoRuido(false)
                .contactoDermal(true)
                .senalPpgCruda("[620, 645, 680, 715, 780, 890, 1020, 840, 710, 630]")
                .build();
        telJuanCritica = telemetriaRepository.save(telJuanCritica);

        AlertaClinica alertaJuan = AlertaClinica.builder()
                .paciente(pacJuan)
                .telemetria(telJuanCritica)
                .fechaHora(telJuanCritica.getFechaHora())
                .tipoAlerta(TipoAlerta.TAQUICARDIA_CRITICA)
                .valorRegistrado("138 ppm")
                .explicacionShap("{\"pulso_ppm\":{\"valor\":\"138 ppm\",\"limite\":\"60 - 100 ppm\",\"shap_value\":0.45},\"imc\":{\"valor\":\"31.4\",\"limite\":\"< 25.0\",\"shap_value\":0.25},\"diabetes\":{\"valor\":\"Presente\",\"limite\":\"No\",\"shap_value\":0.20}}")
                .notificacionEnviada(true)
                .atendida(false)
                .build();
        alertaRepository.save(alertaJuan);

        // Eventos hospitalarios de Juan
        eventoRepository.save(EventoProceso.builder()
                .paciente(pacJuan)
                .actividad("TRIAJE")
                .fechaInicio(OffsetDateTime.now().minusHours(3))
                .fechaFin(OffsetDateTime.now().minusHours(2).minusMinutes(40))
                .ejecutor("Lic. Enfermería")
                .estadoConformidad(EstadoConformidad.CONFORME)
                .build());

        eventoRepository.save(EventoProceso.builder()
                .paciente(pacJuan)
                .actividad("INGRESO_EMERGENCIA")
                .fechaInicio(OffsetDateTime.now().minusHours(2).minusMinutes(35))
                .fechaFin(OffsetDateTime.now().minusHours(1).minusMinutes(30))
                .ejecutor("Dr. Emergenciólogo")
                .estadoConformidad(EstadoConformidad.CONFORME)
                .build());

        // 3. Paciente 2: Riesgo Moderado (Rosa Elena Flores)
        Usuario userRosa = Usuario.builder()
                .correo("rosa.flores@gmail.com")
                .claveHash(passwordEncoder.encode("Paciente123!"))
                .rol(Rol.PACIENTE)
                .activo(true)
                .fechaCreacion(OffsetDateTime.now().minusDays(15))
                .build();
        usuarioRepository.save(userRosa);

        Paciente pacRosa = Paciente.builder()
                .usuario(userRosa)
                .dni("45910238")
                .nombres("Rosa Elena")
                .apellidos("Flores Ramos")
                .fechaNacimiento(LocalDate.of(1972, 8, 24))
                .genero("FEMENINO")
                .edad(53)
                .pesoKg(new BigDecimal("69.0"))
                .tallaCm(158)
                .imc(new BigDecimal("27.6"))
                .obesidad(false)
                .tabaquismo(false)
                .consumoAlcohol(false)
                .diabetes(false)
                .antecedentesCv(true)
                .usoMedicacion(false)
                .hipertension(true)
                .tiroides(true)
                .acv(false)
                .frecuenciaMin(60)
                .frecuenciaMax(95)
                .build();
        pacRosa = pacienteRepository.save(pacRosa);

        ContactoAuxilio conRosa = ContactoAuxilio.builder()
                .paciente(pacRosa)
                .nombreCompleto("Carlos Flores")
                .telefono("912345678")
                .relacion("Esposo")
                .build();
        contactoRepository.save(conRosa);

        TelemetriaSenal telRosa = TelemetriaSenal.builder()
                .paciente(pacRosa)
                .fechaHora(OffsetDateTime.now().minusMinutes(4))
                .pulsoPpm(98)
                .nivelRiesgo(NivelRiesgo.MODERADO)
                .scoreRiesgo(new BigDecimal("0.6450"))
                .artefactoRuido(false)
                .contactoDermal(true)
                .senalPpgCruda("[590, 610, 650, 700, 750, 810, 720, 640, 600]")
                .build();
        telemetriaRepository.save(telRosa);

        // 4. Paciente 3: Estable (Luis Alberto Mendoza)
        Usuario userLuis = Usuario.builder()
                .correo("luis.mendoza@gmail.com")
                .claveHash(passwordEncoder.encode("Paciente123!"))
                .rol(Rol.PACIENTE)
                .activo(true)
                .fechaCreacion(OffsetDateTime.now().minusDays(10))
                .build();
        usuarioRepository.save(userLuis);

        Paciente pacLuis = Paciente.builder()
                .usuario(userLuis)
                .dni("10839201")
                .nombres("Luis Alberto")
                .apellidos("Mendoza Quispe")
                .fechaNacimiento(LocalDate.of(1978, 11, 3))
                .genero("MASCULINO")
                .edad(47)
                .pesoKg(new BigDecimal("74.0"))
                .tallaCm(172)
                .imc(new BigDecimal("25.0"))
                .obesidad(false)
                .tabaquismo(false)
                .consumoAlcohol(false)
                .diabetes(false)
                .antecedentesCv(false)
                .usoMedicacion(false)
                .hipertension(false)
                .tiroides(false)
                .acv(false)
                .frecuenciaMin(60)
                .frecuenciaMax(100)
                .build();
        pacLuis = pacienteRepository.save(pacLuis);

        TelemetriaSenal telLuis = TelemetriaSenal.builder()
                .paciente(pacLuis)
                .fechaHora(OffsetDateTime.now().minusMinutes(1))
                .pulsoPpm(72)
                .nivelRiesgo(NivelRiesgo.NORMAL)
                .scoreRiesgo(new BigDecimal("0.0820"))
                .artefactoRuido(false)
                .contactoDermal(true)
                .senalPpgCruda("[550, 580, 620, 670, 720, 680, 610, 560]")
                .build();
        telemetriaRepository.save(telLuis);

        // 5. Paciente Móvil de la App: Agustín Aguilar (agustin@gmail.com / 12345678)
        Usuario userAgustin = Usuario.builder()
                .correo("agustin@gmail.com")
                .claveHash(passwordEncoder.encode("12345678"))
                .rol(Rol.PACIENTE)
                .activo(true)
                .fechaCreacion(OffsetDateTime.now().minusDays(20))
                .build();
        usuarioRepository.save(userAgustin);

        Paciente pacAgustin = Paciente.builder()
                .usuario(userAgustin)
                .dni("76543210")
                .nombres("Agustín")
                .apellidos("Aguilar")
                .fechaNacimiento(LocalDate.of(1968, 4, 15))
                .genero("MASCULINO")
                .edad(58)
                .pesoKg(new BigDecimal("78.0"))
                .tallaCm(174)
                .imc(new BigDecimal("25.9"))
                .obesidad(false)
                .tabaquismo(false)
                .consumoAlcohol(false)
                .diabetes(false)
                .antecedentesCv(false)
                .usoMedicacion(true)
                .hipertension(true)
                .tiroides(false)
                .acv(false)
                .frecuenciaMin(60)
                .frecuenciaMax(100)
                .build();
        pacAgustin = pacienteRepository.save(pacAgustin);

        ContactoAuxilio conAgustin = ContactoAuxilio.builder()
                .paciente(pacAgustin)
                .nombreCompleto("Elvis Larico")
                .telefono("987654321")
                .relacion("Contacto de auxilio")
                .build();
        contactoRepository.save(conAgustin);

        // Bioseñales de Agustín (coinciden con la app móvil: 76 BPM, reposo)
        TelemetriaSenal telAgustin = TelemetriaSenal.builder()
                .paciente(pacAgustin)
                .fechaHora(OffsetDateTime.now().minusMinutes(5))
                .pulsoPpm(76)
                .nivelRiesgo(NivelRiesgo.NORMAL)
                .scoreRiesgo(new BigDecimal("0.2400"))
                .artefactoRuido(false)
                .contactoDermal(true)
                .senalPpgCruda("[600, 620, 650, 710, 760, 730, 680, 610]")
                .build();
        telAgustin = telemetriaRepository.save(telAgustin);

        // Alerta histórica para Agustín
        AlertaClinica alertaAgustin = AlertaClinica.builder()
                .paciente(pacAgustin)
                .telemetria(telAgustin)
                .fechaHora(OffsetDateTime.now().minusHours(2).minusMinutes(30))
                .tipoAlerta(TipoAlerta.TAQUICARDIA_CRITICA)
                .valorRegistrado("104 ppm")
                .explicacionShap("{\"pulso_ppm\":{\"valor\":\"104 ppm\",\"limite\":\"60 - 100 ppm\",\"shap_value\":0.32},\"imc\":{\"valor\":\"25.9\",\"limite\":\"< 25.0\",\"shap_value\":0.12}}")
                .notificacionEnviada(false)
                .atendida(true)
                .observacionMedica("Frecuencia elevada transitoria. Se recomendó reposo e hidratación.")
                .build();
        alertaRepository.save(alertaAgustin);

        logger.info("¡Sembrado inicial completado exitosamente incluyendo paciente móvil Agustín Aguilar!");
    }

    private void asegurarCuentaCardiologo() {
        final String correoCardiologo = "agustinalex14@gmail.com";
        final String clavePlana = "Botas2311@";

        Optional<Usuario> cardiologoExistente = usuarioRepository.findByCorreo(correoCardiologo);
        if (cardiologoExistente.isPresent()) {
            Usuario u = cardiologoExistente.get();
            u.setClaveHash(passwordEncoder.encode(clavePlana));
            u.setRol(Rol.CARDIOLOGO);
            u.setActivo(true);
            usuarioRepository.save(u);
            logger.info("Credenciales de la cuenta de médico actualizada a: {} / {}", correoCardiologo, clavePlana);
            return;
        }

        // Si existía con el correo anterior cardiologo@hospital.gob.pe, actualizarlo
        Optional<Usuario> antiguo = usuarioRepository.findByCorreo("cardiologo@hospital.gob.pe");
        if (antiguo.isPresent()) {
            Usuario u = antiguo.get();
            u.setCorreo(correoCardiologo);
            u.setClaveHash(passwordEncoder.encode(clavePlana));
            u.setRol(Rol.CARDIOLOGO);
            u.setActivo(true);
            usuarioRepository.save(u);
            logger.info("Cuenta de cardiólogo migrada a {} con contraseña actualizada.", correoCardiologo);
            return;
        }

        // Si no existe, crear la cuenta de médico
        Usuario nuevoCardiologo = Usuario.builder()
                .correo(correoCardiologo)
                .claveHash(passwordEncoder.encode(clavePlana))
                .rol(Rol.CARDIOLOGO)
                .activo(true)
                .fechaCreacion(OffsetDateTime.now().minusDays(90))
                .build();
        usuarioRepository.save(nuevoCardiologo);
        logger.info("Cuenta de médico creada exitosamente: {} / {}", correoCardiologo, clavePlana);
    }

    private void asegurarCuentaPacienteApp() {
        final String clavePlana = "12345678";
        String hash = passwordEncoder.encode(clavePlana);

        // 1. Asegurar cuenta agustin.aguilar@gmail.com con clave 12345678
        Optional<Usuario> userOpt = usuarioRepository.findByCorreo("agustin.aguilar@gmail.com");
        Usuario userAgustin;
        if (userOpt.isPresent()) {
            userAgustin = userOpt.get();
            userAgustin.setClaveHash(hash);
            userAgustin.setActivo(true);
            userAgustin = usuarioRepository.save(userAgustin);
        } else {
            userAgustin = usuarioRepository.save(Usuario.builder()
                    .correo("agustin.aguilar@gmail.com")
                    .claveHash(hash)
                    .rol(Rol.PACIENTE)
                    .activo(true)
                    .fechaCreacion(OffsetDateTime.now().minusDays(30))
                    .build());
        }

        // 2. Asegurar también alias agustin@gmail.com con clave 12345678
        Optional<Usuario> aliasOpt = usuarioRepository.findByCorreo("agustin@gmail.com");
        if (aliasOpt.isEmpty()) {
            usuarioRepository.save(Usuario.builder()
                    .correo("agustin@gmail.com")
                    .claveHash(hash)
                    .rol(Rol.PACIENTE)
                    .activo(true)
                    .fechaCreacion(OffsetDateTime.now().minusDays(30))
                    .build());
        } else {
            Usuario alias = aliasOpt.get();
            alias.setClaveHash(hash);
            alias.setActivo(true);
            usuarioRepository.save(alias);
        }

        // 3. Vincular con paciente Agustín Aguilar
        Optional<Paciente> pacOpt = pacienteRepository.findByDni("14567892");
        if (pacOpt.isPresent()) {
            Paciente p = pacOpt.get();
            p.setUsuario(userAgustin);
            pacienteRepository.save(p);
        }
        logger.info("Cuenta de paciente móvil verificada: agustin.aguilar@gmail.com y agustin@gmail.com / 12345678");
    }

    private void asegurarPacientesMockup() {
        // Los pacientes de prueba se aseguran en el sistema pero TODOS inician en estado DESCONECTADO (sin telemetría en vivo simulada)
        // 1. Sofía Rodríguez Peña (DNI: 48921345)
        Paciente pacSofia = asegurarPacienteConTelemetria(
                "48921345", "Sofía", "Rodríguez Peña", "sofia.rodriguez@gmail.com",
                "FEMENINO", 34, new BigDecimal("64.0"), 165,
                null, NivelRiesgo.NORMAL, BigDecimal.ZERO, false,
                null
        );
        sembrarHistorialPaciente(pacSofia, new int[]{128, 134, 139, 142, 135, 140, 145, 138, 141, 138}, 10);

        // 2. Carlos Mendoza Herrera (DNI: 32114890)
        Paciente pacCarlos = asegurarPacienteConTelemetria(
                "32114890", "Carlos", "Mendoza Herrera", "carlos.mendoza@gmail.com",
                "MASCULINO", 42, new BigDecimal("75.0"), 172,
                null, NivelRiesgo.NORMAL, BigDecimal.ZERO, false,
                null
        );
        sembrarHistorialPaciente(pacCarlos, new int[]{72, 70, 74, 71, 73, 75, 69, 72, 70, 72}, 10);

        // 3. Agustín Aguilar (DNI: 14567892 - Paciente con la app móvil y smartwatch)
        Paciente pacAgustin = asegurarPacienteConTelemetria(
                "14567892", "Agustín", "Aguilar", "agustin.aguilar@gmail.com",
                "MASCULINO", 58, new BigDecimal("82.0"), 162,
                null, NivelRiesgo.NORMAL, BigDecimal.ZERO, false,
                null
        );

        if (pacAgustin != null) {
            pacAgustin.setDiabetes(true);
            pacAgustin.setTabaquismo(false);
            pacAgustin.setObesidad(true);
            pacAgustin.setAntecedentesCv(true);
            pacAgustin.setImc(new BigDecimal("31.0"));
            pacienteRepository.save(pacAgustin);
            sembrarHistorialPaciente(pacAgustin, new int[]{82, 88, 102, 106, 128, 118, 142, 122, 135, 110}, 10);
        }

        // 4. Alejandro Ruiz Diaz (DNI: 29876123)
        asegurarPacienteConTelemetria(
                "29876123", "Alejandro", "Ruiz Diaz", "alejandro.ruiz@gmail.com",
                "MASCULINO", 46, new BigDecimal("78.0"), 175,
                null, NivelRiesgo.NORMAL, BigDecimal.ZERO, false,
                null
        );

        // 5. Lucía Fernández Ortiz (DNI: 41345678)
        Paciente pacLucia = asegurarPacienteConTelemetria(
                "41345678", "Lucía", "Fernández Ortiz", "lucia.fernandez@gmail.com",
                "FEMENINO", 39, new BigDecimal("60.0"), 163,
                null, NivelRiesgo.NORMAL, BigDecimal.ZERO, false,
                null
        );
        sembrarHistorialPaciente(pacLucia, new int[]{66, 68, 67, 70, 65, 69, 68, 67, 68, 68}, 10);

        logger.info("Pacientes verificados. Cero telemetría simulada en vivo sembrada.");
    }

    private void sembrarHistorialPaciente(Paciente pac, int[] pulsos, int diasAtras) {
        if (pac == null) return;
        OffsetDateTime baseDate = OffsetDateTime.now().minusDays(diasAtras);
        for (int i = 0; i < pulsos.length; i++) {
            OffsetDateTime fecha = baseDate.plusDays(i).withHour(10).withMinute(30).withSecond(0).withNano(0);
            if (!telemetriaRepository.existsByPacienteIdPacienteAndFechaHora(pac.getIdPaciente(), fecha)) {
                int pulso = pulsos[i];
                int maxLimite = pac.getFrecuenciaMax() != null ? pac.getFrecuenciaMax() : 100;
                int minLimite = pac.getFrecuenciaMin() != null ? pac.getFrecuenciaMin() : 60;
                boolean esCritico = pulso > maxLimite || pulso < minLimite;
                NivelRiesgo riesgo = esCritico ? NivelRiesgo.CRITICO : NivelRiesgo.NORMAL;
                BigDecimal score = esCritico ? new BigDecimal("0.8500") : new BigDecimal("0.1200");

                TelemetriaSenal t = telemetriaRepository.save(TelemetriaSenal.builder()
                        .paciente(pac)
                        .fechaHora(fecha)
                        .pulsoPpm(pulso)
                        .nivelRiesgo(riesgo)
                        .scoreRiesgo(score)
                        .contactoDermal(true)
                        .artefactoRuido(false)
                        .senalPpgCruda("[600, 620, 680, 750, 840, 780, 690, 610]")
                        .build());

                if (esCritico && i % 2 == 0) {
                    alertaRepository.save(AlertaClinica.builder()
                            .paciente(pac)
                            .telemetria(t)
                            .fechaHora(fecha)
                            .tipoAlerta(pulso > 100 ? TipoAlerta.TAQUICARDIA_CRITICA : TipoAlerta.BRADICARDIA_SEVERA)
                            .valorRegistrado(pulso + " BPM")
                            .explicacionShap("{\"pulso_ppm\":{\"valor\":\"" + pulso + " BPM\",\"limite\":\"" + minLimite + "-" + maxLimite + " BPM\",\"shap_value\":0.40}}")
                            .notificacionEnviada(true)
                            .atendida(false)
                            .observacionMedica("Pico registrado en telemetría continua")
                            .build());
                }
            }
        }
    }

    private Paciente asegurarPacienteConTelemetria(
            String dni, String nombres, String apellidos, String correo,
            String genero, int edad, BigDecimal pesoKg, int tallaCm,
            Integer pulsoPpm, NivelRiesgo riesgo, BigDecimal scoreRiesgo, boolean conectado,
            String tipoAlertaTexto
    ) {
        Optional<Paciente> optPac = pacienteRepository.findByDni(dni);
        Paciente pac;

        if (optPac.isEmpty()) {
            Usuario usuario = usuarioRepository.findByCorreo(correo).orElseGet(() -> {
                Usuario u = Usuario.builder()
                        .correo(correo)
                        .claveHash(passwordEncoder.encode(dni + "@Cardio"))
                        .rol(Rol.PACIENTE)
                        .activo(true)
                        .fechaCreacion(OffsetDateTime.now().minusDays(30))
                        .build();
                return usuarioRepository.save(u);
            });

            BigDecimal tallaM = BigDecimal.valueOf(tallaCm).divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
            BigDecimal imc = pesoKg.divide(tallaM.multiply(tallaM), 2, java.math.RoundingMode.HALF_UP);

            pac = Paciente.builder()
                    .usuario(usuario)
                    .dni(dni)
                    .nombres(nombres)
                    .apellidos(apellidos)
                    .fechaNacimiento(LocalDate.now().minusYears(edad))
                    .edad(edad)
                    .genero(genero)
                    .pesoKg(pesoKg)
                    .tallaCm(tallaCm)
                    .imc(imc)
                    .frecuenciaMin(60)
                    .frecuenciaMax(100)
                    .build();
            pac = pacienteRepository.save(pac);
        } else {
            pac = optPac.get();
        }

        // Cero telemetría simulada al iniciar el servidor: Solo los smartwatches reales activos transmiten telemetría en vivo
        return pac;
    }

    private void asegurarEventosTelemetriaProceso() {
        try {
            jdbcTemplate.execute("ALTER TABLE evento_proceso DROP CONSTRAINT IF EXISTS evento_proceso_estado_conformidad_check");
        } catch (Exception e) {
            logger.warn("Aviso al verificar restricción de evento_proceso: {}", e.getMessage());
        }

        long countActual = eventoRepository.count();
        if (countActual >= 1400) {
            logger.info("EVENTO_PROCESO ya cuenta con {} registros sembrados.", countActual);
            return;
        }

        List<Paciente> todosPacientes = pacienteRepository.findAll();
        if (todosPacientes.isEmpty()) return;

        List<Paciente> criticos = todosPacientes.stream()
                .filter(p -> "48921345".equals(p.getDni()) || "14567892".equals(p.getDni()) || "72849102".equals(p.getDni()))
                .collect(Collectors.toList());
        if (criticos.isEmpty()) criticos = todosPacientes;

        List<Paciente> generales = todosPacientes.stream()
                .filter(p -> !"48921345".equals(p.getDni()) && !"14567892".equals(p.getDni()))
                .collect(Collectors.toList());
        if (generales.isEmpty()) generales = todosPacientes;

        List<EventoProceso> lote = new ArrayList<>(1500);

        // 1. 18 Ciclos Críticos completos (72 eventos) -> 100% SLA Conforme, reacción ~2 min
        for (int i = 0; i < 18; i++) {
            Paciente pac = criticos.get(i % criticos.size());
            OffsetDateTime t0 = OffsetDateTime.now().minusDays(7).plusHours(i * 9 + 1);

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("CAPTURA_INICIADA")
                    .fechaInicio(t0)
                    .ejecutor("Transmisión PPG - App")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("INFERENCIA_IA_COMPLETADA")
                    .fechaInicio(t0.plus(Duration.ofMillis(120)))
                    .ejecutor("Clasificación - FastAPI")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("ALERTA_DESPACHADA")
                    .fechaInicio(t0.plus(Duration.ofMillis(165)))
                    .ejecutor("Notificación - Spring Boot")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("REVISION_MEDICA_CERRADA")
                    .fechaInicio(t0.plus(Duration.ofMillis(165)).plus(Duration.ofMinutes(2)))
                    .ejecutor("Intervención")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());
        }

        // 2. 20 Ciclos Generales completos (80 eventos) -> 17 Conformes (15 min) y 3 Desviaciones (45 min) = 85% SLA
        for (int j = 0; j < 20; j++) {
            Paciente pac = generales.get(j % generales.size());
            OffsetDateTime t0 = OffsetDateTime.now().minusDays(10).plusHours(j * 11 + 2);

            boolean esDesviacion = (j == 2 || j == 7 || j == 14);
            long demoraMin = esDesviacion ? 45 : 15;
            EstadoConformidad estado = esDesviacion ? EstadoConformidad.DESVIACION : EstadoConformidad.CONFORME;

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("CAPTURA_INICIADA")
                    .fechaInicio(t0)
                    .ejecutor("Transmisión PPG - App")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("INFERENCIA_IA_COMPLETADA")
                    .fechaInicio(t0.plus(Duration.ofMillis(120)))
                    .ejecutor("Clasificación - FastAPI")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("ALERTA_DESPACHADA")
                    .fechaInicio(t0.plus(Duration.ofMillis(165)))
                    .ejecutor("Notificación - Spring Boot")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("REVISION_MEDICA_CERRADA")
                    .fechaInicio(t0.plus(Duration.ofMillis(165)).plus(Duration.ofMinutes(demoraMin)))
                    .ejecutor("Intervención")
                    .estadoConformidad(estado)
                    .build());
        }

        // 3. Flujo continuo de telemetría regular (1,268 eventos: 634 pares de captura e inferencia)
        // Total acumulado exacto: 72 + 80 + 1268 = 1,420 eventos
        int paresRestantes = (1420 - lote.size()) / 2;
        OffsetDateTime tBase = OffsetDateTime.now().minusDays(14);
        for (int k = 0; k < paresRestantes; k++) {
            Paciente pac = todosPacientes.get(k % todosPacientes.size());
            OffsetDateTime tStream = tBase.plusMinutes(k * 30);

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("CAPTURA_INICIADA")
                    .fechaInicio(tStream)
                    .ejecutor("Transmisión PPG - App")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());

            lote.add(EventoProceso.builder()
                    .paciente(pac)
                    .actividad("INFERENCIA_IA_COMPLETADA")
                    .fechaInicio(tStream.plus(Duration.ofMillis(120)))
                    .ejecutor("Clasificación - FastAPI")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());
        }

        eventoRepository.saveAll(lote);
        logger.info("¡Sembrados {} eventos de telemetría y minería de procesos en EVENTO_PROCESO!", lote.size());
    }
}
