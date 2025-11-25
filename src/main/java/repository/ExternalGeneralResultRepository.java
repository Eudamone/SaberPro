package repository;

import dto.UniversidadPromedio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT COALESCE(AVG(er.puntGlobal),0) FROM ExternalGeneralResult er")
    Double getPromedyGeneral();

    @Query("SELECT COALESCE(AVG(er.percentilGlobal),0) FROM ExternalGeneralResult er")
    Double getPercentilGeneral();

    @Query(value = """
        select re.inst_nombre_institucion as nombre,(avg(re.punt_global)::float8) as promedio
        from saber_pro.resultado_externo re
        where re.punt_global is not null and re.periodo = :periodo
        group by re.inst_nombre_institucion
        order by promedio desc
        limit 4;
    """,nativeQuery = true)
    List<UniversidadPromedio> getMejoresPromedioUniversidades(@Param("periodo") Integer periodo);

    @Query("select eg.estConsecutivo from ExternalGeneralResult eg where eg.periodo = :periodo and eg.estConsecutivo is not null")
    List<String> findEstConsecutivosByPeriodo(@Param("periodo") Integer periodo);
}