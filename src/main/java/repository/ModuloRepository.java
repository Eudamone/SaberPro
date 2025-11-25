package repository;

import dto.ModuloPromedio;
import model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {
    Optional<Modulo> findFirstByNombreIgnoreCase(String nombre);

    @Query(value = """
        select m.nombre as modulo,
                (avg(rmi.puntaje)::float8) as promedio
        from modulo m
        join resultado_modulo_interno rmi
        	on m.id_modulo = rmi.modulo_id
        join resultado_interno ri
        	on rmi.interno_id = ri.documento and LOWER(public.unaccent(ri.programa)) = LOWER(public.unaccent(:program))
        where LOWER(public.unaccent(m.nombre)) = 'lectura critica'
        	or LOWER(public.unaccent(m.nombre)) = 'comunicacion escrita'
        	or LOWER(public.unaccent(m.nombre)) = 'razonamiento cuantitativo'
        	or LOWER(public.unaccent(m.nombre)) = 'ingles'
        	or LOWER(public.unaccent(m.nombre)) = 'competencias ciudadanas'
        group by m.nombre
        order by m.nombre
    """,nativeQuery = true)
    List<ModuloPromedio> getPromedyModuleByProgram(@Param("program") String program);

    @Query(value = """
        select m.nombre as modulo,
                        (avg(rmi.puntaje)::float8) as promedio
                from saber_pro.modulo m
                join saber_pro.resultado_modulo_interno rmi
                	on m.id_modulo = rmi.modulo_id
                join saber_pro.resultado_interno ri
                	on rmi.interno_id = ri.documento and LOWER(public.unaccent(ri.programa)) = LOWER(public.unaccent(:program))
                	and ri.periodo = :periodo
                where LOWER(public.unaccent(m.nombre)) = 'lectura critica'
                	or LOWER(public.unaccent(m.nombre)) = 'comunicacion escrita'
                	or LOWER(public.unaccent(m.nombre)) = 'razonamiento cuantitativo'
                	or LOWER(public.unaccent(m.nombre)) = 'ingles'
                	or LOWER(public.unaccent(m.nombre)) = 'competencias ciudadanas'
                group by m.nombre
                order by m.nombre;
    """,nativeQuery = true)
    List<ModuloPromedio> getPromedyModuleByProgramForAnio(@Param("program") String program, @Param("periodo") Integer periodo);

    @Query(value = """
        select m.nombre as modulo,
                        (avg(rmi.puntaje)::float8) as promedio
                from saber_pro.modulo m
                join saber_pro.resultado_modulo_interno rmi
                	on m.id_modulo = rmi.modulo_id
                join saber_pro.resultado_interno ri
                	on rmi.interno_id = ri.documento and ri.periodo = :periodo
                where LOWER(public.unaccent(m.nombre)) = 'lectura critica'
                	or LOWER(public.unaccent(m.nombre)) = 'comunicacion escrita'
                	or LOWER(public.unaccent(m.nombre)) = 'razonamiento cuantitativo'
                	or LOWER(public.unaccent(m.nombre)) = 'ingles'
                	or LOWER(public.unaccent(m.nombre)) = 'competencias ciudadanas'
                group by m.nombre
                order by m.nombre;
    """,nativeQuery = true)
    List<ModuloPromedio> getPromedyModuloGeneralForAnio(@Param("periodo") Integer periodo);

    @Query(value = """
        select m.nombre as modulo,
                        (avg(rmi.puntaje)::float8) as promedio
                from saber_pro.modulo m
                join saber_pro.resultado_modulo_interno rmi
                	on m.id_modulo = rmi.modulo_id
                join saber_pro.resultado_interno ri
                	on rmi.interno_id = ri.documento and cast(rmi.interno_id as varchar) = :numIdentification
                where LOWER(public.unaccent(m.nombre)) = 'lectura critica'
                	or LOWER(public.unaccent(m.nombre)) = 'comunicacion escrita'
                	or LOWER(public.unaccent(m.nombre)) = 'razonamiento cuantitativo'
                	or LOWER(public.unaccent(m.nombre)) = 'ingles'
                	or LOWER(public.unaccent(m.nombre)) = 'competencias ciudadanas'
                group by m.nombre
                order by m.nombre;
    """,nativeQuery = true)
    List<ModuloPromedio> getPromedyModuleByStudent(@Param("numIdentification") String numIdentification);

    @Query(value = """
        select *
        from modulo m
        where LOWER(public.unaccent(m.nombre)) = LOWER(public.unaccent(:nombre))
        limit 1
    """, nativeQuery = true)
    Optional<Modulo> findFirstByNombreNormalized(@Param("nombre") String nombre);
}
