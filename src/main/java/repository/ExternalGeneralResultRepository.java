package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExternalGeneralResultRepository extends JpaRepository<model.ExternalGeneralResult, Long> {
    boolean existsByPeriodo(Integer periodo);
    Optional<model.ExternalGeneralResult> findFirstByPeriodo(Integer periodo);
    Optional<model.ExternalGeneralResult> findFirstByPeriodoAndEstConsecutivo(Integer periodo, String estConsecutivo);
}
