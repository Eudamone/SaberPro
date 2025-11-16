package repository;

import model.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartamentoRepository extends JpaRepository<Departamento, Short> {
    Optional<Departamento> findFirstByNombreIgnoreCase(String nombre);
}
