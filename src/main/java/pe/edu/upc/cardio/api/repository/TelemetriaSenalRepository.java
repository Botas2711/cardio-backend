package pe.edu.upc.cardio.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.cardio.api.model.TelemetriaSenal;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TelemetriaSenalRepository extends JpaRepository<TelemetriaSenal, Long> {
    Optional<TelemetriaSenal> findTopByPacienteIdPacienteOrderByFechaHoraDesc(Long idPaciente);

    List<TelemetriaSenal> findByPacienteIdPacienteOrderByFechaHoraDesc(Long idPaciente);

    List<TelemetriaSenal> findByPacienteIdPacienteAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long idPaciente, OffsetDateTime inicio, OffsetDateTime fin);

    long countByPacienteIdPaciente(Long idPaciente);

    boolean existsByPacienteIdPacienteAndFechaHora(Long idPaciente, OffsetDateTime fechaHora);

    @Query("SELECT t FROM TelemetriaSenal t WHERE t.paciente.idPaciente = :idPaciente " +
           "AND t.fechaHora >= :desde ORDER BY t.fechaHora ASC")
    List<TelemetriaSenal> findRecientesPorPaciente(
            @Param("idPaciente") Long idPaciente,
            @Param("desde") OffsetDateTime desde);
}
