package pe.edu.upc.cardio.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pe.edu.upc.cardio.api.model.AlertaClinica;
import pe.edu.upc.cardio.api.model.ContactoAuxilio;
import pe.edu.upc.cardio.api.model.Paciente;
import pe.edu.upc.cardio.api.repository.ContactoAuxilioRepository;
import pe.edu.upc.cardio.api.service.NotificacionAuxilioService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionAuxilioServiceImpl implements NotificacionAuxilioService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionAuxilioServiceImpl.class);
    private final ContactoAuxilioRepository contactoRepository;

    @Override
    public boolean despacharAlertaAuxilio(Paciente paciente, AlertaClinica alerta) {
        List<ContactoAuxilio> contactos = contactoRepository.findByPacienteIdPaciente(paciente.getIdPaciente());

        // HU019 Escenario 2: Ausencia de contacto registrado
        if (contactos.isEmpty()) {
            logger.warn("ADVERTENCIA INTERNA: Se generó alerta crítica para el paciente {} ({}) pero no tiene contacto de auxilio registrado.",
                    paciente.getNombres() + " " + paciente.getApellidos(), paciente.getDni());
            return false;
        }

        // HU019 Escenario 1: Recepción de mensaje de auxilio
        for (ContactoAuxilio contacto : contactos) {
            String mensajeSms = String.format(
                    "ALERTA MÉDICA URGENTE: Su familiar %s %s presenta un evento cardiovascular de alto riesgo (%s - %s). Por favor bríndele asistencia inmediata.",
                    paciente.getNombres(),
                    paciente.getApellidos(),
                    alerta.getTipoAlerta(),
                    alerta.getValorRegistrado()
            );

            logger.info("DESPACHANDO MENSAJE DE AUXILIO vía SMS al teléfono {} (Destinatario: {}): {}",
                    contacto.getTelefono(), contacto.getNombreCompleto(), mensajeSms);
        }

        return true;
    }
}
