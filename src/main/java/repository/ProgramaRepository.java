package repository;

import model.Facultad;
import model.InscripcionId;
import model.Programa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgramaRepository extends JpaRepository<Programa,String> {

    @Query("SELECT p FROM Programa p WHERE p.facultad = :facultad")
    List<Programa> findByFacultad(@Param("facultad") Facultad facultad);

    @Query("SELECT p FROM Programa p WHERE p.name = :name")
    Optional<Programa> findByNamePrograma(@Param("name") String name);

    /**
     * Busca todos los programas académicos usando solo el codigo de la facultad
     * @param codeFaculty El código de la facultad (String).
     * @return Una lista de Programas.
     */
    List<Programa> findByFacultad_CodeFaculty(String codeFaculty);
}
