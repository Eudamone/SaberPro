package repository;

import model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {
    Optional<Modulo> findFirstByNombreIgnoreCase(String nombre);
}
