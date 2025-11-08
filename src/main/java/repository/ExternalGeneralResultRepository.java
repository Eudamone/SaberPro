package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExternalGeneralResultRepository extends JpaRepository<model.ExternalGeneralResult, Long> { //heredamos CRUD de JpaRepository para la entidad ExternalGeneralResult
    boolean existsByPeriodo(Integer periodo); // Verifica si existen registros para un período dado
    Optional<model.ExternalGeneralResult> findFirstByPeriodo(Integer periodo); // Encuentra el primer registro para un período dado
    Optional<model.ExternalGeneralResult> findFirstByPeriodoAndEstConsecutivo(Integer periodo, String estConsecutivo); // Encuentra el primer registro por período y consecutivo
}
