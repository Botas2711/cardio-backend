package pe.edu.upc.cardio.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.dto.triage.PacienteTriageDTO;
import pe.edu.upc.cardio.api.model.AlertaClinica;
import pe.edu.upc.cardio.api.model.NivelRiesgo;
import pe.edu.upc.cardio.api.model.Paciente;
import pe.edu.upc.cardio.api.model.TelemetriaSenal;
import pe.edu.upc.cardio.api.repository.AlertaClinicaRepository;
import pe.edu.upc.cardio.api.repository.PacienteRepository;
import pe.edu.upc.cardio.api.repository.TelemetriaSenalRepository;
import pe.edu.upc.cardio.api.service.TriageService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TriageServiceImpl implements TriageService {

    private final PacienteRepository pacienteRepository;
    private final TelemetriaSenalRepository telemetriaRepository;
    private final AlertaClinicaRepository alertaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PacienteTriageDTO> obtenerTriage(String query, NivelRiesgo filtroRiesgo) {
        List<Paciente> pacientes;

        // HU013 Escenario 2: Búsqueda por DNI o nombres
        if (query != null && !query.trim().isEmpty()) {
            pacientes = pacienteRepository.buscarPorDniONombre(query.trim());
        } else {
            pacientes = pacienteRepository.findAll();
        }

        // Ventana de tiempo para considerar transmisión activa: 10 segundos
        OffsetDateTime limiteReciente = OffsetDateTime.now().minusSeconds(10);

        List<PacienteTriageDTO> triageList = pacientes.stream().map(p -> {
            Optional<TelemetriaSenal> ultimaOpt = telemetriaRepository.findTopByPacienteIdPacienteOrderByFechaHoraDesc(p.getIdPaciente());

            int edad = (p.getFechaNacimiento() != null)
                    ? Period.between(p.getFechaNacimiento(), LocalDate.now()).getYears()
                    : 0;

            Integer ultimoPulso = null;
            NivelRiesgo nivelRiesgo = null;
            BigDecimal scoreRiesgo = BigDecimal.ZERO;
            OffsetDateTime fechaMedicion = null;
            boolean enlaceActivo = false;
            String estadoEnlaceTexto = "Sin transmisión reciente / Dispositivo fuera de línea";

            if (ultimaOpt.isPresent()) {
                TelemetriaSenal u = ultimaOpt.get();
                fechaMedicion = u.getFechaHora();

                // Re-evaluar nivel de riesgo en tiempo real según los umbrales clínicos vigentes del paciente
                int minT = (p.getFrecuenciaMin() != null) ? p.getFrecuenciaMin() : 60;
                int maxT = (p.getFrecuenciaMax() != null) ? p.getFrecuenciaMax() : 100;

                // Un dispositivo está activamente enlazado si transmitió telemetría real en los últimos 10 segundos,
                // con contacto dermal activo y pulso mayor a cero
                boolean esConectado = u.getFechaHora().isAfter(limiteReciente)
                        && Boolean.TRUE.equals(u.getContactoDermal())
                        && u.getPulsoPpm() != null
                        && u.getPulsoPpm() > 0;

                if (esConectado) {
                    enlaceActivo = true;
                    ultimoPulso = u.getPulsoPpm();
                    scoreRiesgo = u.getScoreRiesgo();
                    estadoEnlaceTexto = "Transmisión activa";

                    if (ultimoPulso > maxT || ultimoPulso < minT || ultimoPulso >= 135 || ultimoPulso <= 45) {
                        nivelRiesgo = (ultimoPulso >= 135 || ultimoPulso <= 45 || ultimoPulso > maxT + 15) ? NivelRiesgo.CRITICO : NivelRiesgo.MODERADO;
                    } else {
                        nivelRiesgo = NivelRiesgo.NORMAL;
                    }
                } else {
                    enlaceActivo = false;
                    ultimoPulso = null;
                    nivelRiesgo = null;
                    estadoEnlaceTexto = "Sin transmisión reciente / Dispositivo fuera de línea";
                }
            }

            long alertasPendientes = alertaRepository.findByPacienteIdPacienteOrderByFechaHoraDesc(p.getIdPaciente())
                    .stream().filter(a -> Boolean.FALSE.equals(a.getAtendida())).count();

            return PacienteTriageDTO.builder()
                    .idPaciente(p.getIdPaciente())
                    .dni(p.getDni())
                    .nombres(p.getNombres())
                    .apellidos(p.getApellidos())
                    .genero(p.getGenero())
                    .edad(edad)
                    .ultimoPulso(ultimoPulso)
                    .nivelRiesgoActual(nivelRiesgo)
                    .scoreRiesgo(scoreRiesgo)
                    .ultimaMedicionFechaHora(fechaMedicion)
                    .enlaceActivo(enlaceActivo)
                    .estadoEnlaceTexto(estadoEnlaceTexto)
                    .totalAlertasPendientes(alertasPendientes)
                    .frecuenciaMin(p.getFrecuenciaMin())
                    .frecuenciaMax(p.getFrecuenciaMax())
                    .build();
        }).collect(Collectors.toList());

        // HU013 Escenario 4: Filtro por categoría de riesgo clínico
        if (filtroRiesgo != null) {
            triageList = triageList.stream()
                    .filter(t -> t.getNivelRiesgoActual() == filtroRiesgo)
                    .collect(Collectors.toList());
        }

        // HU013 Escenario 1: Listado priorizado por estado de conexión y nivel de criticidad (descendente: CRITICO -> MODERADO -> NORMAL)
        triageList.sort((a, b) -> {
            boolean aActivo = Boolean.TRUE.equals(a.getEnlaceActivo());
            boolean bActivo = Boolean.TRUE.equals(b.getEnlaceActivo());
            if (aActivo != bActivo) {
                return aActivo ? -1 : 1;
            }
            int pesoA = pesoRiesgo(a.getNivelRiesgoActual());
            int pesoB = pesoRiesgo(b.getNivelRiesgoActual());
            if (pesoA != pesoB) {
                return Integer.compare(pesoB, pesoA);
            }
            // En caso de empate, ordenar por score de riesgo descendente
            BigDecimal sA = a.getScoreRiesgo() != null ? a.getScoreRiesgo() : BigDecimal.ZERO;
            BigDecimal sB = b.getScoreRiesgo() != null ? b.getScoreRiesgo() : BigDecimal.ZERO;
            return sB.compareTo(sA);
        });

        return triageList;
    }

    private int pesoRiesgo(NivelRiesgo riesgo) {
        if (riesgo == null) return 0;
        switch (riesgo) {
            case CRITICO: return 3;
            case MODERADO: return 2;
            case NORMAL: default: return 1;
        }
    }
}
