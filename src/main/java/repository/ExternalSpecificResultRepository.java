package repository;

import model.ExternalSpecificResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalSpecificResultRepository extends JpaRepository<ExternalSpecificResult, Long> {
    List<ExternalSpecificResult> findByEstuConsecutivo(String estuConsecutivo);
}

