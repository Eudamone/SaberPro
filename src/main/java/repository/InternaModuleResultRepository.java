package repository;

import model.InternalModuleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InternaModuleResultRepository extends JpaRepository<InternalModuleResult, Long> {

    @Query("SELECT DISTINCT im.modulo.nombre FROM InternalModuleResult im")
    List<String> getAreas();
}
