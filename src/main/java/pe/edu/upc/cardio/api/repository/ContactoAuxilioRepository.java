package pe.edu.upc.cardio.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.cardio.api.model.ContactoAuxilio;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoAuxilioRepository extends JpaRepository<ContactoAuxilio, Long> {
    List<ContactoAuxilio> findByPacienteIdPaciente(Long idPaciente);
    Optional<ContactoAuxilio> findFirstByPacienteIdPaciente(Long idPaciente);
    Optional<ContactoAuxilio> findByIdContactoAndPacienteIdPaciente(Long idContacto, Long idPaciente);
}
