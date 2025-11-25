package repository;

import model.ExternalModuleResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExternalModuleResultRepository extends JpaRepository<ExternalModuleResult, Long> {

    @Query("select distinct emr.moduloId from ExternalModuleResult emr join emr.externalGeneralResult egr where egr.periodo = :periodo and emr.moduloId is not null")
    List<Integer> findModuleIdsByPeriodo(@Param("periodo") Integer periodo);

}
