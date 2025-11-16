package repository;

import model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    boolean existsByEmail(String email);

    @EntityGraph(
            attributePaths = {"docente.facultad"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    @Query("SELECT u FROM Usuario u WHERE u.id = :id AND u.rol = 'Docente'")
    Optional<Usuario> findDocenteByIdForEdit(@Param("id") Long id);

    @EntityGraph(
            attributePaths = {"estudiante.inscripcion.programa.facultad"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    @Query("SELECT u FROM Usuario u WHERE u.id = :id AND u.rol = 'Estudiante'")
    Optional<Usuario> findEstudianteByIdForEdit(@Param("id") Long id);

    @EntityGraph(
            attributePaths = {"decano.facultad"},
            type = EntityGraph.EntityGraphType.LOAD
    )
    @Query("SELECT u FROM Usuario u WHERE u.id= :id AND u.rol = 'Decano'")
    Optional<Usuario> findDecanoByIdForEdit(@Param("id") Long id);

    @Query("""
        SELECT u FROM Usuario u
    """)
    List<Usuario> findAllUsuarios();

    @Query("""
        SELECT new dto.UsuarioInfoDTO(u.id,u.username,u.nombre,u.email,u.document,u.numIdentification,u.rol)
        FROM Usuario u WHERE u.email = :email
    """)
    Optional<UsuarioInfoDTO> findUsuarioForEmail(@Param("email") String email);

    @Query("""
        SELECT new dto.UsuarioInfoDTO(u.id,u.username,u.nombre,u.email,u.document,u.numIdentification,u.rol)
        FROM Usuario u WHERE u.id = :id
    """)
    Optional<UsuarioInfoDTO> findUserInfo(@Param("id") Long id);

    @Modifying
    @Query("""
        UPDATE Usuario u SET u.pass = :passHash WHERE u.id = :id
    """)
    void updatePassword(@Param("passHash") String passHash,@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(nu) > 0 THEN TRUE ELSE FALSE END "+
            "FROM NewUsuario nu WHERE nu.usuario.id = :id")
    boolean isNewUser(Long id);

    @Modifying
    @Query("DELETE FROM NewUsuario nu WHERE nu.usuario.id = :id")
    void deleteNewUsuarioById(Long id);
}
