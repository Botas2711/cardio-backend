package pe.edu.upc.cardio.api.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.dto.alerta.*;
import pe.edu.upc.cardio.api.dto.recomendacion.RecomendacionDTO;
import pe.edu.upc.cardio.api.dto.recomendacion.RecomendacionesResponseDTO;
import pe.edu.upc.cardio.api.exception.ResourceNotFoundException;
import pe.edu.upc.cardio.api.model.*;
import pe.edu.upc.cardio.api.repository.AlertaClinicaRepository;
import pe.edu.upc.cardio.api.repository.EventoProcesoRepository;
import pe.edu.upc.cardio.api.repository.PacienteRepository;
import pe.edu.upc.cardio.api.service.AlertaService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaServiceImpl implements AlertaService {

    private final AlertaClinicaRepository alertaRepository;
    private final PacienteRepository pacienteRepository;
    private final EventoProcesoRepository eventoProcesoRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> listarAlertasNoAtendidas() {
        return alertaRepository.findByAtendidaFalseOrderByFechaHoraDesc().stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaContadorDTO contarAlertasNoAtendidas() {
        return AlertaContadorDTO.builder()
                .totalNoAtendidas(alertaRepository.countByAtendidaFalse())
                .build();
    }

    @Override
    @Transactional
    public AlertaResponseDTO atenderAlerta(Long idAlerta, AlertaAtenderRequest request) {
        AlertaClinica alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta clínica no encontrada con ID: " + idAlerta));

        alerta.setAtendida(true);
        alerta.setObservacionMedica(request.getObservacionMedica().trim());
        alerta = alertaRepository.save(alerta);

        // Process Mining: Evento 4 - REVISION_MEDICA_CERRADA
        try {
            OffsetDateTime tRevision = OffsetDateTime.now();
            OffsetDateTime tAlerta = (alerta.getFechaHora() != null) ? alerta.getFechaHora() : tRevision.minusMinutes(2);
            long minutosTranscurridos = Math.max(0, Duration.between(tAlerta, tRevision).toMinutes());

            boolean esCritica = alerta.getTipoAlerta() == TipoAlerta.TAQUICARDIA_CRITICA
                    || alerta.getTipoAlerta() == TipoAlerta.BRADICARDIA_SEVERA
                    || alerta.getTipoAlerta() == TipoAlerta.RIESGO_CARDIOVASCULAR_ALTO;
            long limiteSlaMin = esCritica ? 10 : 30;

            EstadoConformidad conformidad = (minutosTranscurridos > limiteSlaMin)
                    ? EstadoConformidad.DESVIACION
                    : EstadoConformidad.CONFORME;

            eventoProcesoRepository.save(EventoProceso.builder()
                    .paciente(alerta.getPaciente())
                    .actividad("REVISION_MEDICA_CERRADA")
                    .fechaInicio(tRevision)
                    .ejecutor("Intervención")
                    .estadoConformidad(conformidad)
                    .build());
        } catch (Exception ex) {
            // Ignorar para no bloquear la atención clínica
        }

        return mapearADto(alerta);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertaExplicacionDTO obtenerExplicacionShap(Long idAlerta) {
        AlertaClinica alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta clínica no encontrada con ID: " + idAlerta));

        Paciente p = alerta.getPaciente();
        TelemetriaSenal t = alerta.getTelemetria();

        List<AlertaExplicacionDTO.FactorShapDTO> factores = new ArrayList<>();

        int pulso = (t != null) ? t.getPulsoPpm() : 75;
        int pulsoMin = (p.getFrecuenciaMin() != null) ? p.getFrecuenciaMin() : 60;
        int pulsoMax = (p.getFrecuenciaMax() != null) ? p.getFrecuenciaMax() : 100;
        double imc = (p.getImc() != null) ? p.getImc().doubleValue() : 24.5;
        int edad = (p.getEdad() != null) ? p.getEdad() : 50;
        boolean diabetes = Boolean.TRUE.equals(p.getDiabetes());
        boolean fumador = Boolean.TRUE.equals(p.getTabaquismo());
        boolean antecedentes = Boolean.TRUE.equals(p.getAntecedentesCv());
        boolean hipertension = Boolean.TRUE.equals(p.getHipertension());
        boolean acv = Boolean.TRUE.equals(p.getAcv());

        // Factor 1: Pulso periférico PPG
        boolean pulsoAnormal = (pulso > pulsoMax || pulso < pulsoMin);
        factores.add(AlertaExplicacionDTO.FactorShapDTO.builder()
                .factor("Frecuencia Cardíaca (Pulso PPG)")
                .valorRegistrado(pulso + " ppm")
                .limiteSeguro(pulsoMin + " - " + pulsoMax + " ppm")
                .pesoShap(pulsoAnormal ? (pulso > pulsoMax ? 0.45 : 0.40) : 0.05)
                .impacto(pulsoAnormal ? "ALTO" : "NORMAL")
                .descripcion(pulso > pulsoMax ? "Taquicardia que incrementa el riesgo hemodinámico." :
                        (pulso < pulsoMin ? "Bradicardia severa fuera del umbral basal seguro." : "Frecuencia de pulso dentro del rango basal."))
                .build());

        // Factor 2: Índice de Masa Corporal (IMC)
        boolean imcElevado = imc >= 25.0;
        factores.add(AlertaExplicacionDTO.FactorShapDTO.builder()
                .factor("Índice de Masa Corporal (IMC)")
                .valorRegistrado(String.format("%.1f kg/m²", imc))
                .limiteSeguro("18.5 - 24.9 kg/m²")
                .pesoShap(imc >= 30 ? 0.25 : (imc >= 25 ? 0.15 : 0.04))
                .impacto(imc >= 30 ? "ALTO" : (imc >= 25 ? "MODERADO" : "NORMAL"))
                .descripcion(imc >= 30 ? "Obesidad grado clínico con sobrecarga metabólica cardiovascular." :
                        (imc >= 25 ? "Sobrepeso moderado." : "IMC dentro de rango normal saludable."))
                .build());

        // Factor 3: Diagnóstico de Diabetes
        factores.add(AlertaExplicacionDTO.FactorShapDTO.builder()
                .factor("Comorbilidad: Diabetes")
                .valorRegistrado(diabetes ? "Diagnóstico Positivo" : "Sin diagnóstico")
                .limiteSeguro("Sin comorbilidad")
                .pesoShap(diabetes ? 0.22 : 0.03)
                .impacto(diabetes ? "ALTO" : "NORMAL")
                .descripcion(diabetes ? "Factor metabólico de alto riesgo aterogénico y vascular." : "Sin presencia de comorbilidad metabólica.")
                .build());

        // Factor 4: Tabaquismo
        factores.add(AlertaExplicacionDTO.FactorShapDTO.builder()
                .factor("Hábito: Tabaquismo")
                .valorRegistrado(fumador ? "Fumador Activo" : "No fumador")
                .limiteSeguro("No fumador")
                .pesoShap(fumador ? 0.18 : 0.02)
                .impacto(fumador ? "MODERADO" : "NORMAL")
                .descripcion(fumador ? "Consumo de tabaco activo que deteriora la respuesta endotelial." : "Sin consumo de tabaco activo.")
                .build());

        // Factor 5: Edad cronológica
        factores.add(AlertaExplicacionDTO.FactorShapDTO.builder()
                .factor("Edad del Paciente")
                .valorRegistrado(edad + " años")
                .limiteSeguro("< 60 años")
                .pesoShap(edad >= 60 ? 0.16 : 0.04)
                .impacto(edad >= 60 ? "MODERADO" : "NORMAL")
                .descripcion(edad >= 60 ? "Grupo etario vulnerable con mayor susceptibilidad cardiovascular." : "Rango etario de menor vulnerabilidad.")
                .build());

        // Factor 6: Antecedentes Familiares Cardíacos
        if (antecedentes || acv) {
            factores.add(AlertaExplicacionDTO.FactorShapDTO.builder()
                    .factor("Antecedentes Cardiovasculares")
                    .valorRegistrado(acv ? "Antecedente de ACV previo" : "Antecedente cardiovascular directo")
                    .limiteSeguro("Sin antecedentes")
                    .pesoShap(acv ? 0.22 : 0.14)
                    .impacto("ALTO")
                    .descripcion("Historial con predisposición severa a eventos coronarios o cerebrovasculares.")
                    .build());
        }

        // Factor 7: Hipertensión
        if (hipertension) {
            factores.add(AlertaExplicacionDTO.FactorShapDTO.builder()
                    .factor("Comorbilidad: Hipertensión")
                    .valorRegistrado("Diagnóstico Positivo")
                    .limiteSeguro("Sin hipertensión")
                    .pesoShap(0.18)
                    .impacto("MODERADO")
                    .descripcion("Resistencia vascular periférica aumentada que eleva la poscarga cardíaca.")
                    .build());
        }

        // Ordenar por impacto / peso SHAP descendente (HU016 Escenario 1)
        factores.sort(Comparator.comparingDouble(AlertaExplicacionDTO.FactorShapDTO::getPesoShap).reversed());

        NivelRiesgo nivel = (t != null) ? t.getNivelRiesgo() : NivelRiesgo.CRITICO;
        double score = (t != null && t.getScoreRiesgo() != null) ? t.getScoreRiesgo().doubleValue() : 0.90;

        String mensajeClinico = (nivel == NivelRiesgo.NORMAL)
                ? "Todos los signos vitales analizados se encuentran dentro de los rangos normales esperados."
                : "La evaluación determinó condición de riesgo impulsada principalmente por: " + factores.get(0).getFactor() + " (" + factores.get(0).getValorRegistrado() + ").";

        return AlertaExplicacionDTO.builder()
                .idAlerta(alerta.getIdAlerta())
                .idPaciente(p.getIdPaciente())
                .nombrePaciente(p.getNombres() + " " + p.getApellidos())
                .nivelRiesgo(nivel)
                .scoreRiesgo(score)
                .estadoEvaluacion(nivel == NivelRiesgo.NORMAL ? "Condición Estable" : "Riesgo Detectado")
                .mensajeClinico(mensajeClinico)
                .factores(factores)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RecomendacionesResponseDTO obtenerRecomendacionesPaciente(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente);
        }

        OffsetDateTime hace30Dias = OffsetDateTime.now().minusDays(30);
        List<AlertaClinica> alertas = alertaRepository.findByPacienteIdPacienteAndFechaHoraAfterOrderByFechaHoraDesc(idPaciente, hace30Dias);

        // HU012 Escenario 2: Historial libre de advertencias recientes
        if (alertas.isEmpty()) {
            return RecomendacionesResponseDTO.builder()
                    .tieneAlertasRecientes(false)
                    .mensaje("No registra alertas recientes. Continúe manteniendo sus hábitos saludables diarios")
                    .recomendaciones(Collections.emptyList())
                    .build();
        }

        // HU012 Escenario 1: Consulta de avisos previos y pautas de autocuidado
        List<RecomendacionDTO> recomendaciones = alertas.stream().map(a -> {
            String pauta;
            switch (a.getTipoAlerta()) {
                case TAQUICARDIA_CRITICA:
                    pauta = "Pautas de autocuidado: Suspenda cualquier actividad extenuante, guarde reposo en posición semisentada, practique respiración diafragmática durante 10 minutos y manténgase hidratado.";
                    break;
                case BRADICARDIA_SEVERA:
                    pauta = "Pautas de autocuidado: Descanse acostado con las piernas ligeramente elevadas, evite movimientos bruscos al ponerse de pie e infórmelo de inmediato a su médico tratante.";
                    break;
                case RIESGO_CARDIOVASCULAR_ALTO:
                default:
                    pauta = "Pautas de autocuidado: Tome asiento de inmediato, guarde reposo preventivo, controle su nivel de estrés y verifique la correcta sujeción dermal de su pulsera inteligente.";
                    break;
            }

            return RecomendacionDTO.builder()
                    .fechaHora(a.getFechaHora())
                    .tipoAlerta(a.getTipoAlerta().name())
                    .valorRegistrado(a.getValorRegistrado())
                    .pautaAutocuidado(pauta)
                    .build();
        }).collect(Collectors.toList());

        return RecomendacionesResponseDTO.builder()
                .tieneAlertasRecientes(true)
                .mensaje("Registro de advertencias detectadas en los últimos 30 días con recomendaciones preventivas")
                .recomendaciones(recomendaciones)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertaResponseDTO> listarPorPaciente(Long idPaciente) {
        return alertaRepository.findByPacienteIdPacienteOrderByFechaHoraDesc(idPaciente).stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    private AlertaResponseDTO mapearADto(AlertaClinica a) {
        return AlertaResponseDTO.builder()
                .idAlerta(a.getIdAlerta())
                .idPaciente(a.getPaciente().getIdPaciente())
                .nombrePaciente(a.getPaciente().getNombres() + " " + a.getPaciente().getApellidos())
                .dniPaciente(a.getPaciente().getDni())
                .fechaHora(a.getFechaHora())
                .tipoAlerta(a.getTipoAlerta())
                .valorRegistrado(a.getValorRegistrado())
                .explicacionShap(a.getExplicacionShap())
                .notificacionEnviada(a.getNotificacionEnviada())
                .atendida(a.getAtendida())
                .observacionMedica(a.getObservacionMedica())
                .build();
    }
}
