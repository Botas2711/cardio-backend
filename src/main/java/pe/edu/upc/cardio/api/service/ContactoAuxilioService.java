package pe.edu.upc.cardio.api.service;

import pe.edu.upc.cardio.api.dto.contacto.ContactoAuxilioRequest;
import pe.edu.upc.cardio.api.dto.contacto.ContactoAuxilioResponseDTO;

import java.util.List;

public interface ContactoAuxilioService {
    List<ContactoAuxilioResponseDTO> listarPorPaciente(Long idPaciente);
    ContactoAuxilioResponseDTO registrarContacto(Long idPaciente, ContactoAuxilioRequest request);
    ContactoAuxilioResponseDTO actualizarContacto(Long idPaciente, Long idContacto, ContactoAuxilioRequest request);
    void eliminarContacto(Long idPaciente, Long idContacto);
}
