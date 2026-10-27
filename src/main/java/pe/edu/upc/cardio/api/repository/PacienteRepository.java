package pe.edu.upc.cardio.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.cardio.api.model.Paciente;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByDni(String dni);
    boolean existsByDni(String dni);
    Optional<Paciente> findByUsuarioIdUsuario(Long idUsuario);
    Optional<Paciente> findByUsuarioCorreo(String correo);

    @Query("SELECT p FROM Paciente p WHERE " +
           "LOWER(p.dni) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.nombres) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Paciente> buscarPorDniONombre(@Param("query") String query);
}
