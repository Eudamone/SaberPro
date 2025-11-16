package repository;

import model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {
    Optional<Ciudad> findFirstByNombreIgnoreCase(String nombre);
}
