package repository;

import model.InternalResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InternalResultRepository extends JpaRepository<InternalResult, Long> {
    Optional<InternalResult> findFirstByPeriodoAndNumeroRegistro(Integer periodo, String numeroRegistro);
}
