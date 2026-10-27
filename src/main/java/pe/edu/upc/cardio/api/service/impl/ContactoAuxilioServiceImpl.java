package pe.edu.upc.cardio.api.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.cardio.api.dto.contacto.ContactoAuxilioRequest;
import pe.edu.upc.cardio.api.dto.contacto.ContactoAuxilioResponseDTO;
import pe.edu.upc.cardio.api.exception.BadRequestException;
import pe.edu.upc.cardio.api.exception.ResourceNotFoundException;
import pe.edu.upc.cardio.api.model.ContactoAuxilio;
import pe.edu.upc.cardio.api.model.Paciente;
import pe.edu.upc.cardio.api.repository.ContactoAuxilioRepository;
import pe.edu.upc.cardio.api.repository.PacienteRepository;
import pe.edu.upc.cardio.api.service.ContactoAuxilioService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactoAuxilioServiceImpl implements ContactoAuxilioService {

    private final ContactoAuxilioRepository contactoRepository;
    private final PacienteRepository pacienteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ContactoAuxilioResponseDTO> listarPorPaciente(Long idPaciente) {
        if (!pacienteRepository.existsById(idPaciente)) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente);
        }
        return contactoRepository.findByPacienteIdPaciente(idPaciente).stream()
                .map(this::mapearADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ContactoAuxilioResponseDTO registrarContacto(Long idPaciente, ContactoAuxilioRequest request) {
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + idPaciente));

        validarTelefono(request.getTelefono());

        ContactoAuxilio contacto = ContactoAuxilio.builder()
                .paciente(paciente)
                .nombreCompleto(request.getNombreCompleto().trim())
                .telefono(request.getTelefono().trim())
                .relacion(request.getRelacion() != null ? request.getRelacion().trim() : "Familiar")
                .build();

        contacto = contactoRepository.save(contacto);
        return mapearADto(contacto);
    }

    @Override
    @Transactional
    public ContactoAuxilioResponseDTO actualizarContacto(Long idPaciente, Long idContacto, ContactoAuxilioRequest request) {
        ContactoAuxilio contacto = contactoRepository.findByIdContactoAndPacienteIdPaciente(idContacto, idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado con ID: " + idContacto + " para este paciente"));

        validarTelefono(request.getTelefono());

        contacto.setNombreCompleto(request.getNombreCompleto().trim());
        contacto.setTelefono(request.getTelefono().trim());
        if (request.getRelacion() != null) {
            contacto.setRelacion(request.getRelacion().trim());
        }

        contacto = contactoRepository.save(contacto);
        return mapearADto(contacto);
    }

    @Override
    @Transactional
    public void eliminarContacto(Long idPaciente, Long idContacto) {
        ContactoAuxilio contacto = contactoRepository.findByIdContactoAndPacienteIdPaciente(idContacto, idPaciente)
                .orElseThrow(() -> new ResourceNotFoundException("Contacto no encontrado con ID: " + idContacto));
        contactoRepository.delete(contacto);
    }

    private void validarTelefono(String telefono) {
        // HU018 Escenario 2: Número de teléfono con formato inválido
        if (telefono == null || !telefono.matches("^9\\d{8}$")) {
            throw new BadRequestException("Número inválido. Ingrese un número telefónico válido");
        }
    }

    private ContactoAuxilioResponseDTO mapearADto(ContactoAuxilio c) {
        return ContactoAuxilioResponseDTO.builder()
                .idContacto(c.getIdContacto())
                .idPaciente(c.getPaciente().getIdPaciente())
                .nombreCompleto(c.getNombreCompleto())
                .telefono(c.getTelefono())
                .relacion(c.getRelacion())
                .build();
    }
}
