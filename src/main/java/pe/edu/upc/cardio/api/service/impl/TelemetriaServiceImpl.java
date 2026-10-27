package pe.edu.upc.cardio.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.dto.fastapi.FastApiPredictRequest;
import pe.edu.upc.cardio.api.dto.fastapi.FastApiPredictResponse;
import pe.edu.upc.cardio.api.dto.telemetria.*;
import pe.edu.upc.cardio.api.exception.BadRequestException;
import pe.edu.upc.cardio.api.exception.ResourceNotFoundException;
import pe.edu.upc.cardio.api.model.*;
import pe.edu.upc.cardio.api.repository.AlertaClinicaRepository;
import pe.edu.upc.cardio.api.repository.EventoProcesoRepository;
import pe.edu.upc.cardio.api.repository.PacienteRepository;
import pe.edu.upc.cardio.api.repository.TelemetriaSenalRepository;
import pe.edu.upc.cardio.api.service.FastApiClientService;
import pe.edu.upc.cardio.api.service.NotificacionAuxilioService;
import pe.edu.upc.cardio.api.service.TelemetriaService;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelemetriaServiceImpl implements TelemetriaService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetriaServiceImpl.class);

    private final TelemetriaSenalRepository telemetriaRepository;
    private final PacienteRepository pacienteRepository;
    private final AlertaClinicaRepository alertaRepository;
    private final EventoProcesoRepository eventoProcesoRepository;
    private final FastApiClientService fastApiClientService;
    private final NotificacionAuxilioService notificacionAuxilioService;

    @Override
    @Transactional
    public TelemetriaResponseDTO procesarIngesta(TelemetriaIngestRequest request) {
        Paciente paciente = pacienteRepository.findById(request.getIdPaciente())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + request.getIdPaciente()));

        OffsetDateTime fechaHora = request.getFechaHora() != null ? request.getFechaHora() : OffsetDateTime.now();
        boolean esRuido = Boolean.TRUE.equals(request.getArtefactoRuido()) || Boolean.FALSE.equals(request.getContactoDermal());

        // Log en tiempo real de telemetría recibida para visualización en terminal
        System.out.printf("[TELEMETRÍA EN VIVO] Paciente: %s %s (ID: %d) | Pulso: %d PPM | SpO2: %d%% | Reloj: %s | Contacto: %s | Bat: %d%%%n",
                paciente.getNombres(), paciente.getApellidos(), paciente.getIdPaciente(),
                request.getPulsoPpm(),
                request.getSpo2Porcentaje() != null ? request.getSpo2Porcentaje() : 98,
                request.getDispositivoNombre() != null ? request.getDispositivoNombre() : "Huawei Band",
                request.getContactoDermal(),
                request.getBateriaPorcentaje() != null ? request.getBateriaPorcentaje() : 100);
        logger.info("[TELEMETRÍA RECIBIDA] Paciente ID: {} | Pulso: {} PPM | SpO2: {}% | Smartwatch: {}",
                request.getIdPaciente(), request.getPulsoPpm(), request.getSpo2Porcentaje() != null ? request.getSpo2Porcentaje() : 98, request.getDispositivoNombre());

        // Process Mining: Evento 1 - CAPTURA_INICIADA
        try {
            eventoProcesoRepository.save(EventoProceso.builder()
                    .paciente(paciente)
                    .actividad("CAPTURA_INICIADA")
                    .fechaInicio(fechaHora)
                    .ejecutor("Transmisión PPG - App")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());
        } catch (Exception ex) {
            logger.warn("Aviso al registrar CAPTURA_INICIADA: {}", ex.getMessage());
        }

        NivelRiesgo nivelRiesgo = NivelRiesgo.NORMAL;
        BigDecimal scoreRiesgo = BigDecimal.ZERO;
        String explicacionShap = null;

        boolean esDesconexion = request.getPulsoPpm() == null || request.getPulsoPpm() <= 0 || Boolean.FALSE.equals(request.getContactoDermal());

        // HU006 y HU007 Escenario 3: Descarte de lectura corrupta o desconexión deliberada
        if (!esRuido && !esDesconexion) {
            int edad = (paciente.getEdad() != null) ? paciente.getEdad() :
                    ((paciente.getFechaNacimiento() != null) ? Period.between(paciente.getFechaNacimiento(), LocalDate.now()).getYears() : 50);

            int sexo = (paciente.getGenero() != null && (paciente.getGenero().equalsIgnoreCase("M") || paciente.getGenero().toUpperCase().startsWith("MASC"))) ? 1 : 0;

            FastApiPredictRequest predictReq = FastApiPredictRequest.builder()
                    .pulsoPpm(request.getPulsoPpm())
                    .edad(edad)
                    .sexo(sexo)
                    .imc(paciente.getImc() != null ? paciente.getImc() : BigDecimal.valueOf(24.5))
                    .diabetes(paciente.getDiabetes())
                    .antecedentesCv(paciente.getAntecedentesCv())
                    .tabaquismo(paciente.getTabaquismo())
                    .obesidad(paciente.getObesidad())
                    .consumoAlcohol(paciente.getConsumoAlcohol())
                    .usoMedicacion(paciente.getUsoMedicacion())
                    .hipertension(paciente.getHipertension())
                    .tiroides(paciente.getTiroides())
                    .acv(paciente.getAcv())
                    .senalPpgCruda(request.getSenalPpgCruda())
                    .build();

            FastApiPredictResponse predictRes = fastApiClientService.predecirRiesgo(predictReq, paciente);

            // Process Mining: Evento 2 - INFERENCIA_IA_COMPLETADA (~120ms latencia)
            try {
                eventoProcesoRepository.save(EventoProceso.builder()
                        .paciente(paciente)
                        .actividad("INFERENCIA_IA_COMPLETADA")
                        .fechaInicio(fechaHora.plus(Duration.ofMillis(120)))
                        .ejecutor("Clasificación - FastAPI")
                        .estadoConformidad(EstadoConformidad.CONFORME)
                        .build());
            } catch (Exception ex) {
                logger.warn("Aviso al registrar INFERENCIA_IA_COMPLETADA: {}", ex.getMessage());
            }

            if (Boolean.TRUE.equals(predictRes.getEsRuido())) {
                esRuido = true;
            } else {
                try {
                    nivelRiesgo = NivelRiesgo.valueOf(predictRes.getNivelRiesgo().toUpperCase());
                } catch (Exception e) {
                    nivelRiesgo = NivelRiesgo.NORMAL;
                }
                scoreRiesgo = predictRes.getScoreRiesgo() != null
                        ? BigDecimal.valueOf(predictRes.getScoreRiesgo())
                        : BigDecimal.valueOf(0.1);
                explicacionShap = predictRes.getExplicacionShap();
            }
        }

        TelemetriaSenal telemetria = TelemetriaSenal.builder()
                .paciente(paciente)
                .fechaHora(fechaHora)
                .pulsoPpm(request.getPulsoPpm())
                .dispositivoNombre(request.getDispositivoNombre())
                .bateriaPorcentaje(request.getBateriaPorcentaje())
                .senalPpgCruda(request.getSenalPpgCruda())
                .nivelRiesgo(nivelRiesgo)
                .scoreRiesgo(scoreRiesgo)
                .artefactoRuido(esRuido)
                .contactoDermal(!Boolean.FALSE.equals(request.getContactoDermal()))
                .build();

        telemetria = telemetriaRepository.save(telemetria);

        // Generar Alerta Clínica y Despacho de Auxilio solo ante mediciones activas válidas
        if (!esRuido && !esDesconexion) {
            boolean superaUmbralMax = (paciente.getFrecuenciaMax() != null && telemetria.getPulsoPpm() > paciente.getFrecuenciaMax());
            boolean superaUmbralMin = (paciente.getFrecuenciaMin() != null && telemetria.getPulsoPpm() < paciente.getFrecuenciaMin());
            boolean esPicoCritico = telemetria.getPulsoPpm() >= 135 || telemetria.getPulsoPpm() <= 45;
            boolean esAlertaCritica = superaUmbralMax || superaUmbralMin || esPicoCritico;

            if (esAlertaCritica) {
                // Alerta crítica inmediata (rate-limit de 5 minutos ante el mismo evento)
                OffsetDateTime hace5Min = OffsetDateTime.now().minusMinutes(5);
                List<AlertaClinica> alertasCriticas = alertaRepository.findByPacienteIdPacienteAndFechaHoraAfterOrderByFechaHoraDesc(
                        paciente.getIdPaciente(), hace5Min);
                if (alertasCriticas.isEmpty()) {
                    generarAlertaClinica(paciente, telemetria, nivelRiesgo, explicacionShap, true);
                }
            } else {
                // Lectura normal / estable (60-100 ppm): registrar alerta de ritmo estable espaciada cada 15 minutos
                OffsetDateTime hace15Min = OffsetDateTime.now().minusMinutes(15);
                List<AlertaClinica> alertasRecientes = alertaRepository.findByPacienteIdPacienteAndFechaHoraAfterOrderByFechaHoraDesc(
                        paciente.getIdPaciente(), hace15Min);
                if (alertasRecientes.isEmpty()) {
                    generarAlertaClinica(paciente, telemetria, nivelRiesgo, explicacionShap, false);
                }
            }
        }

        return mapearADto(telemetria);
    }

    private void generarAlertaClinica(Paciente paciente, TelemetriaSenal telemetria, NivelRiesgo nivelRiesgo, String explicacionShap, boolean esCritica) {
        TipoAlerta tipoAlerta;
        String valorRegistrado = String.format("%d ppm", telemetria.getPulsoPpm());
        String observacionMedica;

        if (esCritica) {
            if (paciente.getFrecuenciaMax() != null && telemetria.getPulsoPpm() > paciente.getFrecuenciaMax()) {
                tipoAlerta = TipoAlerta.TAQUICARDIA_CRITICA;
            } else if (paciente.getFrecuenciaMin() != null && telemetria.getPulsoPpm() < paciente.getFrecuenciaMin()) {
                tipoAlerta = TipoAlerta.BRADICARDIA_SEVERA;
            } else {
                tipoAlerta = TipoAlerta.RIESGO_CARDIOVASCULAR_ALTO;
            }
            observacionMedica = "Lectura: " + valorRegistrado + ". Recomendación: Toma agua y descansa unos minutos";
        } else {
            tipoAlerta = TipoAlerta.RITMO_CARDIACO_ESTABLE;
            observacionMedica = "Lectura: " + valorRegistrado + " | Valores en rango estable";
        }

        AlertaClinica alerta = AlertaClinica.builder()
                .paciente(paciente)
                .telemetria(telemetria)
                .fechaHora(telemetria.getFechaHora())
                .tipoAlerta(tipoAlerta)
                .valorRegistrado(valorRegistrado)
                .observacionMedica(observacionMedica)
                .explicacionShap(explicacionShap)
                .notificacionEnviada(false)
                .atendida(!esCritica)
                .build();

        // HU019: Despacho automático de auxilio familiar SOLO ante alerta crítica
        if (esCritica && (nivelRiesgo == NivelRiesgo.CRITICO || tipoAlerta == TipoAlerta.TAQUICARDIA_CRITICA || tipoAlerta == TipoAlerta.BRADICARDIA_SEVERA)) {
            boolean enviada = notificacionAuxilioService.despacharAlertaAuxilio(paciente, alerta);
            alerta.setNotificacionEnviada(enviada);
        }

        alerta = alertaRepository.save(alerta);

        // Process Mining: Evento 3 - ALERTA_DESPACHADA (+45ms latencia de notificación)
        try {
            eventoProcesoRepository.save(EventoProceso.builder()
                    .paciente(paciente)
                    .actividad("ALERTA_DESPACHADA")
                    .fechaInicio(telemetria.getFechaHora().plus(Duration.ofMillis(165)))
                    .ejecutor("Notificación - Spring Boot")
                    .estadoConformidad(EstadoConformidad.CONFORME)
                    .build());
        } catch (Exception ex) {
            logger.warn("Aviso al registrar ALERTA_DESPACHADA: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional
    public List<TelemetriaResponseDTO> procesarLote(TelemetriaBatchRequest request) {
        // HU008 Escenario 2: Reconexión tras pérdida de enlace (sincronización por lotes)
        List<TelemetriaResponseDTO> resultados = new ArrayList<>();
        for (TelemetriaIngestRequest req : request.getMediciones()) {
            resultados.add(procesarIngesta(req));
        }
        return resultados;
    }

    @Override
    @Transactional(readOnly = true)
    public TelemetriaEnVivoDTO obtenerEnVivo(Long idPaciente) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente));

        Optional<TelemetriaSenal> ultimaOpt = telemetriaRepository.findTopByPacienteIdPacienteOrderByFechaHoraDesc(idPaciente);

        if (ultimaOpt.isEmpty()) {
            return TelemetriaEnVivoDTO.builder()
                    .idPaciente(idPaciente)
                    .esperandoLecturas(true)
                    .estadoMensaje("Esperando lecturas del reloj inteligente...")
                    .sensorConectado(false)
                    .build();
        }

        TelemetriaSenal ultima = ultimaOpt.get();
        OffsetDateTime limiteReciente = OffsetDateTime.now().minusSeconds(10);
        boolean fueraDeLinea = ultima.getFechaHora().isBefore(limiteReciente)
                || Boolean.FALSE.equals(ultima.getContactoDermal())
                || ultima.getPulsoPpm() == null
                || ultima.getPulsoPpm() <= 0;
        boolean esperandoLecturas = fueraDeLinea;

        int minT = (paciente.getFrecuenciaMin() != null) ? paciente.getFrecuenciaMin() : 60;
        int maxT = (paciente.getFrecuenciaMax() != null) ? paciente.getFrecuenciaMax() : 100;

        boolean superaUmbral = !fueraDeLinea && ultima.getPulsoPpm() != null && (ultima.getPulsoPpm() > maxT || ultima.getPulsoPpm() < minT);
        boolean esPicoFisiologico = !fueraDeLinea && ultima.getPulsoPpm() != null && (ultima.getPulsoPpm() >= 135 || ultima.getPulsoPpm() <= 45);
        boolean esRiesgo = superaUmbral || esPicoFisiologico;

        String estadoMensaje;
        NivelRiesgo nivelRiesgoActualizado;
        if (esperandoLecturas) {
            estadoMensaje = "Esperando lecturas del reloj inteligente...";
            nivelRiesgoActualizado = NivelRiesgo.NORMAL;
        } else if (esRiesgo) {
            estadoMensaje = "Estado: Riesgo detectado, guarde reposo";
            nivelRiesgoActualizado = (esPicoFisiologico || (ultima.getPulsoPpm() != null && ultima.getPulsoPpm() > maxT + 15))
                    ? NivelRiesgo.CRITICO : NivelRiesgo.MODERADO;
        } else {
            estadoMensaje = "Estado: Estable";
            nivelRiesgoActualizado = NivelRiesgo.NORMAL;
        }

        return TelemetriaEnVivoDTO.builder()
                .idPaciente(idPaciente)
                .pulsoPpm(fueraDeLinea ? null : ultima.getPulsoPpm())
                .nivelRiesgo(nivelRiesgoActualizado)
                .estadoMensaje(estadoMensaje)
                .esperandoLecturas(fueraDeLinea)
                .sensorConectado(!fueraDeLinea)
                .fechaHora(ultima.getFechaHora())
                .dispositivoNombre(fueraDeLinea ? "Desconectado" : ultima.getDispositivoNombre())
                .bateriaPorcentaje(fueraDeLinea ? null : ultima.getBateriaPorcentaje())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Resumen24hDTO obtenerResumen24h(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente);
        }

        OffsetDateTime hace24h = OffsetDateTime.now().minusHours(24);
        List<TelemetriaSenal> lecturas = telemetriaRepository.findRecientesPorPaciente(idPaciente, hace24h);

        List<PuntoGraficoDTO> puntos = lecturas.stream()
                .map(l -> PuntoGraficoDTO.builder()
                        .fechaHora(l.getFechaHora())
                        .pulsoPpm(l.getPulsoPpm())
                        .nivelRiesgo(l.getNivelRiesgo())
                        .build())
                .collect(Collectors.toList());

        // HU010 Escenario 2: Si el paciente comenzó a usar el dispositivo hace menos de 1 hora o tiene pocas lecturas
        boolean suficientesDatos = lecturas.size() >= 5;
        String mensaje = suficientesDatos
                ? "Historial de 24 horas continuo"
                : "Acumulando lecturas para completar su resumen de 24 horas";

        double promedioPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).average().orElse(0.0);
        int maxPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).max().orElse(0);
        int minPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).min().orElse(0);

        return Resumen24hDTO.builder()
                .puntos(puntos)
                .suficientesDatos(suficientesDatos)
                .mensaje(mensaje)
                .promedioPulso(Math.round(promedioPulso * 10.0) / 10.0)
                .maxPulso(maxPulso)
                .minPulso(minPulso)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BloqueHorarioDTO obtenerPorBloqueHorario(Long idPaciente, String bloque, LocalDate fecha) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente);
        }

        LocalDate targetDate = (fecha != null) ? fecha : LocalDate.now(ZoneId.of("America/Lima"));
        LocalTime horaInicio;
        LocalTime horaFin;
        String nombreBloque = (bloque != null ? bloque.trim().toUpperCase() : "MAÑANA");

        switch (nombreBloque) {
            case "TARDE":
                horaInicio = LocalTime.of(12, 0);
                horaFin = LocalTime.of(17, 59, 59);
                break;
            case "NOCHE":
                horaInicio = LocalTime.of(18, 0);
                horaFin = LocalTime.of(23, 59, 59);
                break;
            case "MADRUGADA":
                horaInicio = LocalTime.of(0, 0);
                horaFin = LocalTime.of(5, 59, 59);
                break;
            case "MAÑANA":
            case "MANANA":
            default:
                nombreBloque = "MAÑANA";
                horaInicio = LocalTime.of(6, 0);
                horaFin = LocalTime.of(11, 59, 59);
                break;
        }

        ZoneId localZone = ZoneId.of("America/Lima");
        OffsetDateTime start = targetDate.atTime(horaInicio).atZone(localZone).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime end = targetDate.atTime(horaFin).atZone(localZone).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);

        List<TelemetriaSenal> lecturas = telemetriaRepository.findByPacienteIdPacienteAndFechaHoraBetweenOrderByFechaHoraAsc(
                idPaciente, start, end);

        // HU011 Escenario 2: Período sin lecturas registradas
        if (lecturas.isEmpty()) {
            return BloqueHorarioDTO.builder()
                    .bloque(nombreBloque)
                    .intervalo(horaInicio + " - " + horaFin)
                    .totalRegistros(0)
                    .mensaje("No se registran datos en este intervalo de tiempo")
                    .lecturas(Collections.emptyList())
                    .build();
        }

        double promPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).average().orElse(0.0);

        List<PuntoGraficoDTO> puntos = lecturas.stream()
                .map(l -> PuntoGraficoDTO.builder()
                        .fechaHora(l.getFechaHora())
                        .pulsoPpm(l.getPulsoPpm())
                        .nivelRiesgo(l.getNivelRiesgo())
                        .build())
                .collect(Collectors.toList());

        return BloqueHorarioDTO.builder()
                .bloque(nombreBloque)
                .intervalo(horaInicio + " - " + horaFin)
                .totalRegistros(lecturas.size())
                .promedioPulso(Math.round(promPulso * 10.0) / 10.0)
                .mensaje("Lecturas correspondientes al bloque horario de " + nombreBloque.toLowerCase())
                .lecturas(puntos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoResumenDTO obtenerHistorico(Long idPaciente, String agrupacion, LocalDate inicio, LocalDate fin) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente);
        }

        LocalDate fechaFin = (fin != null) ? fin : LocalDate.now();
        LocalDate fechaInicio = (inicio != null) ? inicio : fechaFin.minusDays(7);

        OffsetDateTime start = fechaInicio.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = fechaFin.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC);

        List<TelemetriaSenal> lecturas = telemetriaRepository.findByPacienteIdPacienteAndFechaHoraBetweenOrderByFechaHoraAsc(
                idPaciente, start, end);

        // HU015 Escenario 2: Intervalo temporal sin registros
        if (lecturas.isEmpty()) {
            return HistoricoResumenDTO.builder()
                    .agrupacion(agrupacion != null ? agrupacion.toUpperCase() : "DIARIO")
                    .totalRegistros(0)
                    .mensaje("No existen registros en el intervalo seleccionado")
                    .mediciones(Collections.emptyList())
                    .build();
        }

        double promPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).average().orElse(0.0);
        int maxPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).max().orElse(0);
        int minPulso = lecturas.stream().mapToInt(TelemetriaSenal::getPulsoPpm).min().orElse(0);

        List<PuntoGraficoDTO> mediciones = lecturas.stream()
                .map(l -> PuntoGraficoDTO.builder()
                        .fechaHora(l.getFechaHora())
                        .pulsoPpm(l.getPulsoPpm())
                        .nivelRiesgo(l.getNivelRiesgo())
                        .build())
                .collect(Collectors.toList());

        return HistoricoResumenDTO.builder()
                .agrupacion(agrupacion != null ? agrupacion.toUpperCase() : "DIARIO")
                .totalRegistros(lecturas.size())
                .promedioPulso(Math.round(promPulso * 10.0) / 10.0)
                .maxPulso(maxPulso)
                .minPulso(minPulso)
                .mensaje("Historial clínico obtenido satisfactoriamente")
                .mediciones(mediciones)
                .build();
    }

    private TelemetriaResponseDTO mapearADto(TelemetriaSenal t) {
        return TelemetriaResponseDTO.builder()
                .idTelemetria(t.getIdTelemetria())
                .idPaciente(t.getPaciente().getIdPaciente())
                .fechaHora(t.getFechaHora())
                .pulsoPpm(t.getPulsoPpm())
                .dispositivoNombre(t.getDispositivoNombre())
                .bateriaPorcentaje(t.getBateriaPorcentaje())
                .nivelRiesgo(t.getNivelRiesgo())
                .scoreRiesgo(t.getScoreRiesgo())
                .artefactoRuido(t.getArtefactoRuido())
                .contactoDermal(t.getContactoDermal())
                .senalPpgCruda(t.getSenalPpgCruda())
                .build();
    }
}
