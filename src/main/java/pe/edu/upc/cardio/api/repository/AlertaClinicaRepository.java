package pe.edu.upc.cardio.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.cardio.api.model.AlertaClinica;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AlertaClinicaRepository extends JpaRepository<AlertaClinica, Long> {
    List<AlertaClinica> findByPacienteIdPacienteOrderByFechaHoraDesc(Long idPaciente);

    List<AlertaClinica> findByAtendidaFalseOrderByFechaHoraDesc();

    long countByAtendidaFalse();

    List<AlertaClinica> findByPacienteIdPacienteAndAtendidaTrueAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long idPaciente, OffsetDateTime inicio, OffsetDateTime fin);

    List<AlertaClinica> findByPacienteIdPacienteAndFechaHoraAfterOrderByFechaHoraDesc(
            Long idPaciente, OffsetDateTime desde);
}
