package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExternalGeneralResultRepository extends JpaRepository<model.ExternalGeneralResult, Long> { //heredamos CRUD de JpaRepository para la entidad ExternalGeneralResult
    boolean existsByPeriodo(Integer periodo); // Verifica si existen registros para un período dado
    Optional<model.ExternalGeneralResult> findFirstByPeriodo(Integer periodo); // Encuentra el primer registro para un período dado
    Optional<model.ExternalGeneralResult> findFirstByPeriodoAndEstConsecutivo(Integer periodo, String estConsecutivo); // Encuentra el primer registro por período y consecutivo
    Optional<model.ExternalGeneralResult> findByEstConsecutivo(String estConsecutivo); // Encuentra un registro por consecutivo

    @Query(value = """
        select distinct re.estuNucleoPregrado
        from ExternalGeneralResult re
        where re.estuPrgmAcademico = :nombrePrograma
        and re.estuNucleoPregrado  != '[NULL]'
    """)
    List<String> findNBC(String nombrePrograma);

    @Query("""
        SELECT DISTINCT eg.estuNucleoPregrado FROM ExternalGeneralResult eg
        WHERE eg.estuPrgmAcademico = :nombrePrograma AND eg.estuPrgmAcademico != '[NULL]' AND eg.estuPrgmAcademico IS NOT NULL 
    """)
    List<String> getNBCs(String nombrePrograma);
}