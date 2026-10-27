package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.model.AlertaClinica;
import pe.edu.upc.cardio.api.model.Paciente;

public interface NotificacionAuxilioService {
    boolean despacharAlertaAuxilio(Paciente paciente, AlertaClinica alerta);
}
