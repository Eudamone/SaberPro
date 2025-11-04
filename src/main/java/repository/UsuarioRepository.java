package repository;

import model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import dto.UsuarioInfoDTO;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    interface Credentials{
        Long getId();
        String getPass();
        String getUsername();
        String getRol();
    }

    Optional<Credentials> findCredentialsByUsername(String username);

    @Query("""
        SELECT new dto.UsuarioInfoDTO(u.id,u.username,u.nombre,u.email,u.document,u.numIdentification,u.rol)
        FROM Usuario u
    """)
    List<UsuarioInfoDTO> findUsuarioInfo();

    @EntityGraph(
            attributePaths = {
                    "decano",
                    "docente",
                    "estudiante"
            },
            type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Usuario> findByUsername(String username);


    boolean existsByUsername(String username);

    @EntityGraph(
            attributePaths = {
                    "docente.facultad",
                    "decano.facultad",
                    // Se carga la colección de programas
                    "estudiante.inscripcion.programa.facultad",
            },
            type = EntityGraph.EntityGraphType.LOAD
    )
    @Query("SELECT u FROM Usuario u WHERE u.id = :id")
    Optional<Usuario> findByIdForEdit(@Param("id") Long id);

    @Query("""
        SELECT u FROM Usuario u
    """)
    List<Usuario> findAllUsuarios();
}
