package pe.edu.upc.cardio.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.cardio.api.model.EstadoConformidad;
import pe.edu.upc.cardio.api.model.EventoProceso;
import pe.edu.upc.cardio.api.model.NivelRiesgo;

import java.util.List;

@Repository
public interface EventoProcesoRepository extends JpaRepository<EventoProceso, Long> {
    List<EventoProceso> findByPacienteIdPacienteOrderByFechaInicioAsc(Long idPaciente);

    List<EventoProceso> findAllByOrderByFechaInicioAsc();

    long countByActividad(String actividad);

    long countByActividadAndEstadoConformidad(String actividad, EstadoConformidad estadoConformidad);

    List<EventoProceso> findByActividadOrderByFechaInicioAsc(String actividad);

    List<EventoProceso> findByActividadAndEstadoConformidad(String actividad, EstadoConformidad estadoConformidad);

    @Query("SELECT e FROM EventoProceso e WHERE e.paciente.idPaciente IN " +
           "(SELECT DISTINCT t.paciente.idPaciente FROM TelemetriaSenal t WHERE t.nivelRiesgo = :nivelRiesgo) " +
           "ORDER BY e.fechaInicio ASC")
    List<EventoProceso> findByNivelRiesgoPaciente(@Param("nivelRiesgo") NivelRiesgo nivelRiesgo);

    @Query("SELECT COUNT(e) FROM EventoProceso e WHERE e.actividad = :actividad AND e.paciente.idPaciente IN " +
           "(SELECT DISTINCT t.paciente.idPaciente FROM TelemetriaSenal t WHERE t.nivelRiesgo = :nivelRiesgo)")
    long countByActividadAndNivelRiesgoPaciente(@Param("actividad") String actividad, @Param("nivelRiesgo") NivelRiesgo nivelRiesgo);

    @Query("SELECT COUNT(e) FROM EventoProceso e WHERE e.actividad = :actividad AND e.estadoConformidad = :estado AND e.paciente.idPaciente IN " +
           "(SELECT DISTINCT t.paciente.idPaciente FROM TelemetriaSenal t WHERE t.nivelRiesgo = :nivelRiesgo)")
    long countByActividadAndEstadoAndNivelRiesgoPaciente(@Param("actividad") String actividad, @Param("estado") EstadoConformidad estado, @Param("nivelRiesgo") NivelRiesgo nivelRiesgo);
}
