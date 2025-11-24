package repository;

import dto.InternResultInfo;
import dto.MejorModulo;
import dto.PromedioAnioDTO;
import dto.PromedioProgram;
import model.InternalResult;
import org.hibernate.annotations.DialectOverride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InternalResultRepository extends JpaRepository<InternalResult, Long>,InternalResultRepositoryCustom {
    Optional<InternalResult> findFirstByPeriodoAndNumeroRegistro(Integer periodo, String numeroRegistro);


    @Query("SELECT COUNT(*) FROM InternalResult")
    Integer sizeInternResultsAll();

    @Query("SELECT DISTINCT ir.periodo FROM InternalResult ir order by ir.periodo asc ")
    List<Integer> getPeriods();

    @Query("""
        SELECT new dto.PromedioAnioDTO(ir.periodo,AVG(ir.puntajeGlobal)) 
        FROM InternalResult ir
        WHERE ir.puntajeGlobal > 0
        GROUP BY ir.periodo
        ORDER BY ir.periodo asc
    """)
    List<PromedioAnioDTO> getPromedyForAnio();

    @Query(value = """
        select
        	p.nombre as programa,
        	(avg(ri.puntaje_global)::float8) as promedio
        from programa p
        join resultado_interno ri
        	on lower(public.unaccent(p.nombre )) = lower(public.unaccent(ri.programa))
        where p.cod_facultad = :codeFaculty and ri.puntaje_global > 0
        group by p.nombre
    """,nativeQuery = true)
    List<PromedioProgram> getPromedyProgramsFaculty(@Param("codeFaculty") String codeFaculty);

    @Query(value = """
        select (avg(ri.puntaje_global)::float8) from programa p
        join resultado_interno ri
        on  lower(public.unaccent(p.nombre)) = lower(public.unaccent(ri.programa))
        where p.cod_facultad = :codeFaculty and ri.puntaje_global > 0
        group by p.cod_facultad
    """,nativeQuery = true)
    Double getPromedyGeneralByFacultad(@Param("codeFaculty") String codeFaculty);

    @Query(value = """
        select (avg(ri.percentil_nacional_global)::float8) from programa p
        join resultado_interno ri
        on  lower(public.unaccent(p.nombre)) = lower(public.unaccent(ri.programa))
        where p.cod_facultad = :codeFaculty and ri.puntaje_global > 0
        group by p.cod_facultad
    """,nativeQuery = true)
    Double getPercentilGeneralFacultadDean(@Param("codeFaculty") String codeFaculty);

    @Query("SELECT DISTINCT ir.semestre FROM InternalResult ir WHERE ir.semestre IS NOT NULL ORDER BY ir.semestre ASC")
    List<Integer> getSemesters();

    // métodos resultados estudiantes

    @Query(value = """
        select ri.periodo from resultado_interno ri
        join usuario u
        on cast(ri.documento as varchar) = :numIdentification
        group by ri.periodo;
    """,nativeQuery = true)
    Integer getPeriodoByStudent(@Param("numIdentification") String numIdentification);

    @Query(value = """
        select p.nombre  from usuario u
        join estudiante e
        on u.id_user = e.id_user
        join inscripcion i
        on e.id_user = i.id_user
        join programa p
        on i.cod_programa = p.cod_programa
        where u.numero_identificacion = :numIdentification;
    """,nativeQuery = true)
    String getProgramaStudent(@Param("numIdentification") String numIdentification);

    @Query("SELECT COUNT(*) FROM InternalResult ir WHERE ir.periodo = :periodo")
    Integer sizeInternResultsByAnio(@Param("periodo") Integer periodo);

    @Query(value = """
        select count(*) from resultado_interno ri
        where lower(public.unaccent(ri.programa)) = lower(public.unaccent(:programa))
        and ri.periodo = :periodo;
    """,nativeQuery = true)
    Integer sizeInternalResultsByPrograma(@Param("periodo") Integer periodo,@Param("programa") String programa);

    @Query(value = """
        select distinct cast(posicion as int)
        from (
        	select ri.documento::varchar as documento,
        	rank() over (order by ri.puntaje_global desc) as posicion
        	from saber_pro.resultado_interno ri
        	where ri.periodo = :periodo
        )as sub
        where sub.documento = :numIdentification;
    """,nativeQuery = true)
    Integer getPuestoUniversidadByAnio(@Param("periodo") Integer periodo, @Param("numIdentification") String numIdentification);

    @Query(value = """
        select distinct cast(posicion as int)
                from (
                	select ri.documento::varchar as documento,
                	rank() over (order by ri.puntaje_global desc) as posicion
                	from saber_pro.resultado_interno ri
                	where ri.periodo = :periodo
                	and lower(public.unaccent(ri.programa)) = lower(public.unaccent(:programa))
                )as sub
                where sub.documento = :numIdentification;
    """,nativeQuery = true)
    Integer getPuestoProgramaByAnio(@Param("periodo") Integer periodo, @Param("numIdentification") String numIdentification,@Param("programa") String programa);

    @Query(value = """
        select distinct on (m.nombre) m.nombre as nombre, rmi.puntaje as puntaje
        from saber_pro.resultado_interno ri
        join saber_pro.resultado_modulo_interno rmi
        on ri.documento = rmi.interno_id
        join saber_pro.modulo m
        on rmi.modulo_id = m.id_modulo
        where cast(ri.documento as varchar) = :numIdentification
        order by m.nombre, rmi.puntaje desc
        limit 1;
    """,nativeQuery = true)
    MejorModulo getMejorModuloStudent(@Param("numIdentification") String numIdentification);

    @Query(value = """
        select distinct ri.percentil_nacional_global
        from saber_pro.resultado_interno ri
        where cast(ri.documento as varchar) = :numIdentification;
    """,nativeQuery = true)
    Integer getPercentilNacionalStudent(@Param("numIdentification") String numIdentification);

    @Query(value = """
        select distinct ri.puntaje_global
        from saber_pro.resultado_interno ri
        where cast(ri.documento as varchar) = :numIdentification;
    """,nativeQuery = true)
    Integer getPuntajeGlobalStudent(@Param("numIdentification") String numIdentification);
}