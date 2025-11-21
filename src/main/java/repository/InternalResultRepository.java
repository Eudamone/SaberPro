package repository;

import dto.InternResultInfo;
import model.InternalResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InternalResultRepository extends JpaRepository<InternalResult, Long>, InternalResultRepositoryCustom {
    Optional<InternalResult> findFirstByPeriodoAndNumeroRegistro(Integer periodo, String numeroRegistro);

    @Query("""
        select  new dto.InternResultInfo(ir.periodo,ir.nombre,ir.numeroRegistro,ir.programa,ir.puntajeGlobal)
        from InternalResult ir
        order by ir.id
    """)
    Page<InternResultInfo> findAllResults(Pageable pageable);

    @Query("SELECT COUNT(*) FROM InternalResult")
    Integer sizeInternResultsAll();

    @Query("SELECT DISTINCT ir.periodo FROM InternalResult ir order by ir.periodo asc ")
    List<Integer> getPeriods();

}